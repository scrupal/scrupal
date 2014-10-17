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
 * We avoid using ng-app='scrupal' so that angular doesn't attempt to process that directive before we've loaded
 * our scrupal module. We utilize the domReady module to make sure that the DOM really is ready before we
 * instruct Angular to go bootstrap on it. This should give us a clean, reliable, flicker-free startup for
 * Scrupal pages.
 */
define(['require'], function (require) {
    'use strict';

    /** Ask RequireJS to ensure that domReady, AngularJS and scrupal itself are loaded before proceeding */
    require(['domReady', 'angular', 'scrupal/scrupal'], function (domReady, ng ) {
        /** Invoke domReady as a wrapper around the Angular bootstrapping so we don't go off half-cocked */
        domReady(function() {

            /** We always bootstrap the scrupal module into the document. This gives us a variety of tools that are
             * shared across other Angular modules. For example, the marked.js module is loaded by scrupal and made
             * available with the <marked></marked> elements. This is a fundamental capability we want for all
             * Scrupal pages, so it goes in the scrupal module and therefore does not need to be specially loaded
             * by other modules.
             */
            ng.bootstrap(window.document, ['scrupal']);

            /** In the angularPage.scala.html template we defined the angular/scrupal module that the page wants to
             * use. This basically sets up a require/scrupal/angular module as a one-page-ap. The module is specified
             * as the window variable `scrupal_module_to_load`. So, if we find that value, we load the corresponding
             * module and bootstrap it to the element with the same ID. Easy Peasy. :)
             */
            if ('scrupal_module_to_load' in window) {
                var mod = window.scrupal_module_to_load
                if (mod !== 'scrupal') {
                    require(['/assets/javascripts/' + mod + '/' + mod + '.js'], function() {
                        var body_selector = '#' + mod
                        ng.bootstrap( window.document.body.querySelector(body_selector), [mod])
                    })
                }
            }
        })
    });
});
