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

define(['angular', 'marked'], function(angular, marked) {

    var mod = angular.module('apidoc.controllers', [])

    mod.controller('Intro', ['$scope', '$http', '$routeParams', '$compile',
        function ApiDocIntroCntl($scope, $http, $routeParams, $compile )  {
            $http.get("/doc/api").
                success(function(data, status, headers) {
                    /* Find the element we want to replace with a query selector */
                    var elem = window.document.getElementById("index-placeholder");
                    var par = elem.parent
                    /* Now replace that the data we got  */
                    elem.html(data)

                    /* Get angular to process this block */
                    var fnLink = $compile(elem);     // returns a Link function used to bind template to the scope
                    var content = fnLink($scope);
                    render()
                    alert('content=" + content');
                }).
                error(function(data,status, headers) {
                    alert("Failed with: " + status + " " + headers + " data=" + data)
                })
        }
    ]);

    mod.controller('Top', ['$scope', '$http', '$routeParams',
        function ApiDocTopCntl($scope, $http, $routeParams) {
            switch ($routeParams.method) {
                case "GET": {
                    $http.get("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                case "PUT": {
                    $http.put("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                case "POST": {
                    $http.post("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                case "HEAD" : {
                    $http.header("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                case "DELETE" : {
//                    $http.delete("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                case "OPTIONS" : {
                    $http.put("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
                    break;
                }
                default : {
                    break;
                }
            }
            $http.get("/doc/api/modules/" + $routeParams.modName).success(function(data) { $scope.module = data })
        }
    ]);

    mod.controller('Kind', ['$scope', '$http',
        function ScrupalSitesController($scope, $http)  {
            $http.get("/api/sites").success(function(data) { $scope.sites = data })
        }
    ]);

    mod.controller('Item', ['$scope', '$http', '$routeParams',
        function ScrupalModuleController($scope, $http, $routeParams) {
            $http.get("/api/sites/" + $routeParams.modName).success(function(data) { $scope.site = data })
        }
    ])

    mod.controller('Trait', ['$scope', '$http',
        function ScrupalEntitiesController($scope, $http)  {
            $http.get("/api/entities").success(function(data) { $scope.entities = data })
        }
    ]);

});
