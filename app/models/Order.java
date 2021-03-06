package models;

import org.geolatte.geom.Point;

import javax.persistence.*;
import java.util.Set;

@Entity(name = "orders")
public class Order extends BaseEntity{

    @JoinColumn(name = "project_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;

    @JoinColumn(name = "customer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @Column
    private Point customerPosition;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderProduct> orderProducts;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    @OrderBy("updateAt DESC ")
    private Set<Mission> missions;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<OrderProduct> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(Set<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }

    public Set<Mission> getMissions() {
        return missions;
    }

    public Order setMissions(Set<Mission> missions) {
        this.missions = missions;
        return this;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public Point getCustomerPosition() {
        return customerPosition;
    }

    public void setCustomerPosition(Point customerPosition) {
        this.customerPosition = customerPosition;
    }
}
