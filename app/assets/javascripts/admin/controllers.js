/*
 * This file is part of Scrupal a Web Application Framework.
 *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.
 *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.
 */

'use strict';

/* Controllers */

define(['angular'], function(angular) {

    var mod = angular.module('admin.controllers', [])

    mod.controller('Scrupal', ['$scope', '$http', function ScrupalAdminController($scope, $http) {
        // $http.get('phones/phones.json').success(function(data) { $scope.data = data }
        $scope.owner = "Owner Name"
    }]);

    mod.controller('Modules', ['$scope', '$http', function ScrupalModulesController($scope, $http)  {
        $http.get("/api/modules").success(function(data) { $scope.modules = data })
    }]);
    mod.controller('Module', ['$scope', '$http', '$routeParams', function ScrupalModuleController($scope, $http, $routeParams) {
        $http.get("/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
    }])

    mod.controller('Sites', ['$scope', '$http', function ScrupalSitesController($scope, $http)  {
        $http.get("/api/sites").success(function(data) { $scope.sites = data })
    }]);
    mod.controller('Site', ['$scope', '$http', '$routeParams', function ScrupalModuleController($scope, $http, $routeParams) {
        $http.get("/api/sites/" + $routeParams.modName).success(function(data) { $scope.site = data })
    }])

    mod.controller('Entities', ['$scope', '$http', function ScrupalEntitiesController($scope, $http)  {
        $http.get("/api/entities").success(function(data) { $scope.entities = data })
    }]);
    mod.controller('Entity', ['$scope', '$http', '$routeParams', function ScrupalModuleController($scope, $http, $routeParams) {
        $http.get("/api/entities/" + $routeParams.modName).success(function(data) { $scope.entity = data })
    }])

    mod.controller('Traits', ['$scope', '$http', function ScrupalTraitsController($scope, $http)  {
        $http.get("/api/traits").success(function(data) { $scope.traits = data })
    }]);
    mod.controller('Entity', ['$scope', '$http', '$routeParams', function ScrupalModuleController($scope, $http, $routeParams) {
        $http.get("/api/traits/" + $routeParams.modName).success(function(data) { $scope.trait = data })
    }])

    mod.controller('Types', ['$scope', '$http', function ScrupalTypesController($scope, $http)  {
        $http.get("/api/types").success(function(data) { $scope.types = data })
    }]);
    mod.controller('Type', ['$scope', '$http', '$routeParams', function ScrupalModuleController($scope, $http, $routeParams) {
        $http.get("/api/types/" + $routeParams.modName).success(function(data) { $scope.type = data })
    }])

});
