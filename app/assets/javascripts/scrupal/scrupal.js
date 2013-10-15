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
    './services.js',
    'webjars!ui-bootstrap.js',
    'webjars!ui-bootstrap-tpls.js'
], function (ng, controllers, directives, filters, services) {
    'use strict';

    /**
     * Declare the scrupal module. This is fundamental to Scrupal applications and this module provides all the basic
     * elements and capabilities that any Scrupal application needs. What it doesn't do is provide any particular
     * application level constructs, just facilities applications can utilize.
     */
    var scrupal = ng.module('scrupal', [
        'ui.bootstrap',
        'ui.bootstrap.tpls',
        'scrupal.services',
        'scrupal.controllers',
        'scrupal.filters',
        'scrupal.directives'
    ])

    scrupal.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {

        $locationProvider.html5Mode(true)

    }]);

    return scrupal
});
