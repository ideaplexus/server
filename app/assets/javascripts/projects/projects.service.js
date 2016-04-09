(function () {
    angular.module('ProjectsApp').service('ProjectsService', ['$http', function ($http) {

        this.projectUrl = "/projects/";

        this.loadProject = function (id) {
            return $http.get(this.projectUrl + id)
                .then(function (response) {
                    return response.data;
                }).catch(function (e) {
                    console.log("Failed while loading project", e);
                    return {};
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