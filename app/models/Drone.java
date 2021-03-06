package models;

import com.vividsolutions.jts.geom.Coordinate;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity(name = "drones")
public class Drone extends BaseEntity {

    private String name;
    private UUID token;

    @Column(name = "last_known_position")
    private Coordinate lastKnownPosition;

    private Integer payload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "drone")
    @OrderBy("clientTime DESC")
    private Set<DroneInfo> droneInfos;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "current_mission_id")
    private Mission currentMission;

    @Column
    private Boolean isActive;

    public Coordinate getLastKnownPosition() {
        return lastKnownPosition;
    }

    public void setLastKnownPosition(Coordinate lastKnownPosition) {
        this.lastKnownPosition = lastKnownPosition;
    }

    public Integer getPayload() {
        return payload;
    }

    public void setPayload(Integer payload) {
        this.payload = payload;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Set<DroneInfo> getDroneInfos() {
        return droneInfos;
    }

    public void setDroneInfos(Set<DroneInfo> droneInfos) {
        this.droneInfos = droneInfos;
    }

    public Mission getCurrentMission() {
        return currentMission;
    }

    public void setCurrentMission(Mission currentMission) {
        this.currentMission = currentMission;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    @Transient
    public double getRemainingBatteryPercent() {
        if (droneInfos != null && !droneInfos.isEmpty()) {
            DroneInfo latestDroneInfo = droneInfos.stream().findFirst().get();
            return latestDroneInfo.getRemainingBatteryPercent();
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Drone{" +
                "name='" + name + '\'' +
                ", token=" + token +
                ", lastKnownPosition=" + lastKnownPosition +
                ", payload=" + payload +
                ", organisation=" + organisation +
                ", project=" + project +
                ", droneInfos=" + droneInfos +
                ", currentMission=" + currentMission +
                ", isActive=" + isActive +
                '}';
    }
}
