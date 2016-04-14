package controllers;

import com.google.inject.Inject;
import commons.AbstractIntegrationTest;
import dao.OrganisationsDao;
import dao.ProjectsDao;
import models.Organisation;
import models.Project;
import org.fluentlenium.core.filter.Filter;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Optional;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.withId;

public class ProjectsControllerTest extends AbstractIntegrationTest {

    @Inject
    private ProjectsDao projectsDao;

    @Inject
    private OrganisationsDao organisationsDao;


    @Test
    public void shouldShowNewProject() {
        Project project = createNewProject();

        browser.goTo(routes.ProjectsController.index().url());

        // verify
        assertThat(browser.pageSource()).containsIgnoringCase(project.getName());
    }

    @Test
    public void shouldRemoveOrganisation() {
        Project project = createNewProject();

        browser.goTo(routes.ProjectsController.index().url());
        assertThat(browser.pageSource()).contains(project.getName());

        // remove that
        browser.click(withId("delete-" + project.getId().toString()));

        waitAndClick("deleteconfirm-" + project.getId().toString());

        // verify
        browser.goTo(routes.ProjectsController.index().url());
        assertThat(browser.pageSource()).doesNotContain(project.getName());
    }


    private Project createNewProject() {

        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("First Demo");

        jpaApi.withTransaction(() -> {
            Optional<Organisation> first = organisationsDao.findAll().stream().findFirst();
            assertThat(first.isPresent()).isTrue();

            project.setOrganisation(first.get());
            projectsDao.persist(project);
        });

        return project;
    }
}