package controllers.api;

import com.google.inject.Inject;
import commons.SessionHelper;
import dao.ProjectsDao;
import mappers.ProjectMapper;
import models.Organisation;
import models.Project;
import models.Zone;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectsApiController extends Controller {

    @Inject
    private ProjectsDao projectsDao;

    @Inject
    private SessionHelper sessionHelper;

    @Inject
    private ProjectMapper projectMapper;

    @Transactional
    public Result index() {
        Organisation organisation = sessionHelper.getOrganisation(session());

        List<Project> projects = projectsDao.findByOrganisation(organisation.getId());

        List<ProjectDto> projectDtos = projects.stream().map(projectMapper::getProjectDto).collect(Collectors.toList());
        return ok(Json.toJson(projectDtos));
    }

    @Transactional
    public Result show(UUID projectID) {
        Project found = projectsDao.findById(projectID);
        if (found == null) {
            return forbidden("Project not found for id " + projectID.toString());
        }

        return ok(Json.toJson(projectMapper.getProjectDto(found)));
    }

    @Transactional
    public Result updateOrInsert(UUID projectId) {
        Project project = projectsDao.findById(projectId);

        boolean isNewProject = project == null;
        if (isNewProject) {
            // create new project
            project = new Project();
            project.setId(UUID.randomUUID());
            project.setOrganisation(sessionHelper.getOrganisation(session()));
        }

        ProjectDto fromRequest =
            Json.fromJson(request().body().asJson(), ProjectDto.class);
        // set all fields
        project.setName(fromRequest.getName());
        addZonesToProject(project, fromRequest);
        projectsDao.persist(project);

        return ok();
    }

    /**
     * This needs a special handling - because we need to check if
     * there are already saved sones, or new ones.
     */
    private void addZonesToProject(Project project,
                                   ProjectDto fromRequest) {

        Set<Zone> zones = project.getZones();
        HashSet<Zone> previousZones = new HashSet<>(zones);

        /**
         * we cannot set a new HashSet because, jpa need to track changes
         */
        project.getZones().clear();
        project.getZones().addAll(mapAll(fromRequest, project, previousZones));
    }

    private List<Zone> mapAll(ProjectDto fromRequest,
                              Project project,
                              Set<Zone> previousZones) {

        return fromRequest
            .getZones()
            .stream()
            .map(zoneDto -> {
                Zone zone = new Zone();
                zone.setId(zoneDto.getId());

                if(previousZones.contains(zone)){
                    zone = findById(previousZones, zoneDto);
                }

                zone.setType(zoneDto.getType());
                zone.setHeight(zoneDto.getHeight());
                zone.setPolygon(zoneDto.getPolygon());
                zone.setName(zoneDto.getName());
                zone.setProject(project);
                return zone;
            }).collect(Collectors.toList());
    }

    private Zone findById(Set<Zone> previousZones, ZoneDto zoneDto) {
        return previousZones.stream().filter(o -> o.getId().equals(zoneDto.getId())).findFirst().get();
    }
}
