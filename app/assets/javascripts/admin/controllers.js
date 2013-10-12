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
        $scope.modules = [
            { name : "Core", description: "The Scrupal Core Module", version: "0.1.0" },
            { name : "Foo", description: "Some made up thing", version: "0.0.0" }
        ]
    }]);

    mod.controller('Sites', ['$scope', '$http', function ScrupalSitesController($scope, $http)  {
        $scope.sites = { One: { domain : 'one.site.org' }, Two: { domain: 'two.fer.com' } }
    }]);

    mod.controller('Entities', ['$scope', '$http', function ScrupalEntitiesController($scope, $http)  {
        $scope.entities = { Core: { version : '0.1.0' }, Foo: { version: '0.0.0' } }
    }]);

    mod.controller('Traits', ['$scope', '$http', function ScrupalTraitsController($scope, $http)  {
        $scope.traits= { Core: { version : '0.1.0' }, Foo: { version: '0.0.0' } }
    }]);

    mod.controller('Types', ['$scope', '$http', function ScrupalTypesController($scope, $http)  {
        $scope.types = { Core: { version : '0.1.0' }, Foo: { version: '0.0.0' } }
    }]);

});
