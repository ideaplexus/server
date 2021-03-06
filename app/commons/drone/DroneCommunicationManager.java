package service.drone;

import ch.helin.messages.converter.JsonBasedMessageConverter;
import ch.helin.messages.dto.message.Message;
import service.SettingsHelper;
import dao.DroneDao;
import models.Drone;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DroneCommunicationManager {

    private final DroneMessageDispatcher droneMessageDispatcher;

    @Inject
    private JsonBasedMessageConverter messageConverter;

    @Inject
    private SettingsHelper settingsHelper;

    private final Map<UUID, DroneConnection> droneIdToConnection = new ConcurrentHashMap<>();

    @Inject
    public DroneCommunicationManager(DroneDao droneDao,
                                     JPAApi jpaApi,
                                     DroneMessageDispatcher droneMessageDispatcher,
                                     SettingsHelper settingsHelper) {

        this.settingsHelper = settingsHelper;
        this.droneMessageDispatcher = droneMessageDispatcher;
        jpaApi.withTransaction(() -> {
            droneDao.findAll().stream().forEach(this::addDrone);
        });

    }

    public void addDrone(Drone drone) {
        droneIdToConnection.put(drone.getId(), new DroneConnection(drone, droneMessageDispatcher, settingsHelper));
    }

    public void sendMessageToDrone(UUID droneId, Message message) {
        DroneConnection droneConnection = droneIdToConnection.get(droneId);
        String messageAsString = messageConverter.parseMessageToString(message);
        droneConnection.sendMessage(messageAsString);
    }
}
