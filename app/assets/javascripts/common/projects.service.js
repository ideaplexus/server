(function () {
    angular.module('common').service('ProjectsService', ['$http', function ($http) {

        this.projectUrl = "/api/projects/";

        this.loadProjects = function () {
            return $http.get(this.projectUrl)
                .then(function (response) {
                    return response.data;
                }).catch(function (error) {
                    return error;
                });
        };
        
        this.loadProject = function (id) {
            return $http.get(this.projectUrl + id)
                .then(function (response) {
                    return response.data;
                }).catch(function (error) {
                    //Do nothing because it could be a new Project
                    return error;
                });
        };

        this.saveProject = function (project) {
            return $http.post(this.projectUrl + project.id + '/update', project)
                .then(function (response) {
                    return response.data;
                })
                .catch(function (error) {
                    console.log("Failed to save ...", response);
                    return error;
                });
        };

    }])
}());
