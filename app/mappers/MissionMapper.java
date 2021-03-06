package mappers;

import ch.helin.messages.dto.DroneInfoDto;
import ch.helin.messages.dto.MissionDto;
import com.google.inject.Inject;
import models.Mission;

import java.util.List;
import java.util.stream.Collectors;

public class MissionMapper {

    @Inject
    private RouteMapper routeMapper;

    @Inject
    private DroneInfoMapper droneInfoMapper;

    @Inject
    private OrderProductsMapper orderProductsMapper;

    public MissionDto convertToMissionDto (Mission mission) {
        MissionDto missionDto = new MissionDto();

        missionDto.setId(mission.getId());
        missionDto.setRoute(routeMapper.convertToRouteDto(mission.getRoute()));
        missionDto.setOrderProduct(orderProductsMapper.convertToOrderProductDto(mission.getOrderProduct()));

        if (mission.getDroneInfos() != null) {
            List<DroneInfoDto> droneInfosDto = mission.getDroneInfos()
                .stream()
                .map(droneInfoMapper::convertToDroneInfoDto)
                .collect(Collectors.toList());

            missionDto.setDroneInfos(droneInfosDto);
        }
        if (mission.getState() != null) {
            missionDto.setState(mission.getState().name());
        }

        return missionDto;
    }


}
