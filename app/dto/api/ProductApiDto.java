package dto.api;

public class ProductApiDto {
    private String id;
    private String name;
    private Double price;
    private String projectId;

    private OrganisationApiDto organisation;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setProjectId(String project) {
        this.projectId = project;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public OrganisationApiDto getOrganisation() {
        return organisation;
    }

    public void setOrganisation(OrganisationApiDto organisation) {
        this.organisation = organisation;
    }
}
