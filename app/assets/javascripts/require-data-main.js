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

/** Configure requireJs
 * See [http://requirejs.org/docs/api.html#config] for details
 */
require.config({
    baseUrl : '/assets/javascripts',

    paths: {
        'jquery'                : [ '/webjars/lib/jquery/jquery'],
        'domReady'              : [ '/webjars/lib/requirejs-domready/domReady'],
        'requirejs'             : [ '/webjars/lib/requirejs/require' ],
        'marked'                : [ '/webjars/lib/marked/marked' ],
        'angular'               : [ '/webjars/lib/angularjs/angular' ],
        'ngRoute'               : [ '/webjars/lib/angularjs/angular-route'],
        'ng.ui'                 : [ '/webjars/lib/angular-ui/angular-ui'],
        'ng.DragAndDrop'        : [ '/webjars/lib/angular-dragdrop/draganddrop' ],
        'ng.MultiSelect'        : [ '/webjars/lib/angular-multi-select/angular-multi-select'],
        'ng.ui.bootstrap'       : [ '/webjars/lib/angular-ui-bootstrap/ui-bootstrap'] ,
        'ng.ui.bootstrap.tpls'  : [ '/webjars/lib/angular-ui-bootstrap/ui-bootstrap-tpls'] ,
        'ng.ui.calendar'        : [ '/webjars/lib/angular-ui-calendar/calendar'],
        'ng.ui.router'          : [ '/webjars/lib/angular-ui-router/angular-ui-router'],
        'ng.ui.Utils'           : [ '/webjars/lib/angular-ui-utils/ui-utils'],
        'jqueryUI'              : [ '/webjars/lib/jquery-ui/ui/jquery-ui'],
        'scrupal'               : [ 'scrupal'],
        'apidoc'                : [ 'apidoc' ],
        'admin'                 : [ 'admin' ]
    },

    /**
     * Shims are dependency settings for things that don't grok requirejs.
     */
    shim: {
        'jquery'    : { exports: '$' },
        'marked'    : { exports: 'marked' },
        'angular'   : { exports: 'angular' },
        'ngRoute'   : { exports: 'ngRoute' }
        // 'jsRoutes'  : { exports: 'jsRoutes' },
        // 'domReady'  : { exports: 'domReady' }
    },

    /** This is the really important part. RequireJS reads this file first because it is the only javascript file
     * referenced from the `<script> ` tag in plainPage.scala.html. Instead of risking script load ordering issues, we
     * do not put the ng-app directive in the HTML file. Instead we tell requireJs that it depends on
     * bootstrap-angular.js which it will load as part of processing this configuration. That javascript is what
     * bootstrap's AngularJS, not the ng-app directive.
     */
    deps: ['/assets/javascripts/bootstrap-angular.js']
});

/** Create names for the various javascripts we use so we can depend on them more easily. */
// define('angular',       ['/webjars/lib/angularjs/angular'], function(ng) { return ng; });
// define('nguibootstrap', ['/webjars/lib/angular-ui-bootstrap/ui-bootstrap'], function(uiboot) { return uiboot; });

/* define('jsroutes',      ['assets/javascripts/jsroutes.js'],function(jsRoutes) { return jsRoutes; });
define('jquery',        ['webjars!jquery.js'],              function(jquery) { return jquery; });
define('angular-ui',    ['webjars!angular-ui.js'],          function(angularUI) { return angularUI; });
define('ui-router',     ['webjars!angular-ui-router.js'],   function(uiRouter) { return uiRouter; });
define('ui-bootstrap',  ['webjars!angular-ui-bootstrap.js'],function(uiBootstrap) { return uiBootstrap; });
define('marked',        ['webjars!marked.js'],              function(marked) { return marked; });
define('domReady'       ['webjars!domReady.js'],            function(domReady) { return domReady; });
   */
