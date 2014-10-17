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

define([
    'angular',
    './controllers.js',
    './directives.js',
    './filters.js',
    './services.js'
], function (ng) {
    'use strict';

    var apidoc = ng.module('apidoc', [
        'apidoc.filters',
        'apidoc.services',
        'apidoc.directives',
        'apidoc.controllers'
    ])

    apidoc.config(['$routeProvider', function($routeProvider) {

        $routeProvider
            .when('/', {
                templateUrl: 'chunks/apidoc/intro.html',
                controller: 'Intro'
            })
            .when('/:method', {
                templateUrl: 'chunks/apidoc/top.html',
                controller: 'Top'
            })
            .when( '/:method/:kind', {
                templateUrl: 'chunks/apidoc/kind.html',
                controller: 'Kind'
            })
            .when('/:method/:kind/', {
                templateUrl: 'chunks/apidoc/kind.html',
                controller: 'Kind'
            })
            .when('/:method/:kind/:id', {
                templateUrl: 'chunks/apidoc/item.html',
                controller: 'Item'
            })
            .when('/:method/:kind/:id/:trait', {
                templateUrl: 'chunks/apidoc/trait.html',
                controller: 'Trait'
            })
            .otherwise({redirectTo: '/'})
    }])

    // Tell angular where to bootstrap this application
    ng.bootstrap(document.body.querySelector('#apidoc'),['apidoc']);

    return apidoc
});

