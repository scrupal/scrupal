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

/**
 * Bootstrap angular onto the window.document node so we don't have to deal with asynchronous loading issues.
 * We avoid using ng-app="scrupal" so that angular doesn't attempt to process that directive before we've loaded
 * our scrupal module. We utilize the domReady module to make sure that the DOM really is ready before we
 * instruct Angular to go bootstrap on it. This should give us a clean, reliable, flicker-free startup for
 * Scrupal pages.
 */
define(['require'], function (require) {
    'use strict';


    /** Ask RequireJS to ensure that domReady, AngularJS and scrupal itself are loaded before proceeding */
    require(['webjars!domReady', 'angular', '/assets/javascripts/scrupal/scrupal.js'], function (domReady, ng, scrupal ) {
        /** Invoke domReady as a wrapper around the Angular bootstrapping so we don't go off half-cocked */
        domReady(function() {
            ng.bootstrap(window.document, ['scrupal']);
        })
    });
});
