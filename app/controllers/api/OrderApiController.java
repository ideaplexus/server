package controllers.api;

import ch.helin.messages.commons.AssertUtils;
import ch.helin.messages.dto.way.RouteDto;
import com.google.gson.Gson;
import com.google.inject.Inject;
import commons.order.MissionDispatchingService;
import commons.routeCalculationService.RouteCalculationService;
import dao.*;
import dto.api.OrderApiDto;
import dto.api.OrderProductApiDto;
import mappers.RouteMapper;
import models.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@Transactional
public class OrderApiController extends Controller {

    private static final Logger logger = getLogger(OrderApiController.class);

    @Inject
    private MissionDispatchingService missionDispatchingService;

    @Inject
    private OrderDao orderDao;

    @Inject
    private RouteMapper routeMapper;

    @Inject
    private CustomerDao customerDao;

    @Inject
    private ProjectsDao projectsDao;

    @Inject
    private ProductsDao productsDao;

    @Inject
    private MissionsDao missionsDao;

    @Inject
    private RouteCalculationService routeCalculationService;

    /*
     * An Order with mission and route is created,
     * but it should not be sent to the drone.
     * The customer should receive an offer for
     * the deliveryLocation first
     */
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public Result create() {
        String jsonNode = request().body().asJson().toString();
        OrderApiDto orderApiDto = parseJsonOrNull(jsonNode);

        if (orderApiDto == null) {
            logger.debug("Send wrong request back, because invalid json: {}", jsonNode);
            return forbidden("Wrong request");
        }

        Customer customer = saveCustomer(orderApiDto);
        Order order = saveOrder(orderApiDto, customer);

        Route route = routeCalculationService.calculateRoute(orderApiDto.getCustomerPosition(), order.getProject());

        addMissionsToOrder(order, route);
        orderDao.persist(order);

        RouteDto routeDto = routeMapper.convertToRouteDto(route);
        return ok(Json.toJson(routeDto));
    }

    private OrderApiDto parseJsonOrNull(String rawJson) {
        try {
            return new Gson().fromJson(rawJson, OrderApiDto.class);
        } catch (Exception e) {
            logger.warn("Failed to parse json {}", rawJson, e);
            return null;
        }
    }

    private Customer saveCustomer(OrderApiDto orderApiDto) {
        Customer customer = new Customer();
        customer.setDisplayName(orderApiDto.getDisplayName());
        customer.setEmail(orderApiDto.getEmail());

        // TODO fix this -> see HEL 54
        customer.setToken(RandomStringUtils.randomAlphanumeric(10));
        customerDao.persist(customer);

        return customer;
    }

    private Order saveOrder(OrderApiDto orderApiDto, Customer customer) {
        Project project =
            projectsDao.findById(UUID.fromString(orderApiDto.getProjectId()));
        AssertUtils.throwExceptionIfNull(project, "Project not found");

        Order order = new Order();
        order.setCustomer(customer);
        order.setProject(project);
        order.setState(OrderState.ROUTE_SUGGESTED);
        order.setOrderProducts(getOrderProducts(orderApiDto, order));

        orderDao.persist(order);
        return order;
    }

    private void addMissionsToOrder(Order order, Route route) {
        Set<Mission> createdMissions =
            order.getOrderProducts()
                .stream()
                .map((orderProduct) -> {
                    Mission mission = new Mission();
                    mission.setOrder(order);
                    mission.setState(MissionState.NEW);
                    mission.setOrderProduct(orderProduct);
                    mission.setRoute(route);

                    route.setMission(mission);
                    mission.setRoute(route);
                    return mission;
                })
                .collect(Collectors.toSet());

        order.setMissions(createdMissions);
    }

    private Set<OrderProduct> getOrderProducts(OrderApiDto orderApiDto, Order newOrder) {
        HashSet<OrderProduct> orderProducts = new HashSet<>();
        List<OrderProductApiDto> orderProductDtos = orderApiDto.getOrderProducts();

        for (OrderProductApiDto orderedProduct : orderProductDtos) {
            Product product = productsDao.findById(UUID.fromString(orderedProduct.getProductId()));
            AssertUtils.throwExceptionIfNull(product, "Product not found");

            Integer orderedAmount = orderedProduct.getAmount();
            boolean orderedAmountFitsOnOneDrone = orderedAmount < product.getMaxItemPerDrone();
            if (orderedAmountFitsOnOneDrone) {
                orderProducts.add(createOrderProduct(newOrder, product, orderedAmount));
            } else {

                // we need to split the order
                int neededOrderProducts = orderedAmount / product.getMaxItemPerDrone();
                for (int i = 0; i < neededOrderProducts; i++) {
                    orderProducts.add(createOrderProduct(newOrder, product, product.getMaxItemPerDrone()));
                }

                // for the remaining items
                int remaining = orderedAmount % product.getMaxItemPerDrone();
                boolean thereAreRestItems = remaining != 0;
                if (thereAreRestItems) {
                    orderProducts.add(createOrderProduct(newOrder, product, remaining));
                }
            }
        }

        return orderProducts;
    }

    private OrderProduct createOrderProduct(Order newOrder,
                                            Product product,
                                            Integer amount) {

        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setAmount(amount);
        orderProduct.setOrder(newOrder);
        orderProduct.setProduct(product);
        orderProduct.setTotalPrice(product.getPrice() * amount);
        return orderProduct;
    }

    /*
     * An existing Order is set as confirmed
     * and the mission is sent to drone
     */
    public Result confirm(UUID orderID) {
        Order order = orderDao.findById(orderID);
        if (order == null) {
            return forbidden("Order not found");
        }

        order.setState(OrderState.IN_PROGRESS);
        order.getMissions().stream().forEach(mission -> {
            mission.setState(MissionState.WAITING_FOR_FREE_DRONE);
            missionsDao.persist(mission);
        });
        orderDao.persist(order);

        missionDispatchingService.tryToDispatchWaitingMissions(order.getProject().getId());

        return ok();
    }

}
