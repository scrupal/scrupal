/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

/** Configure requireJs
 * See [http://requirejs.org/docs/api.html#config] for details
 */
require.config({
    baseUrl : '/assets/javascripts',

    paths: {
        'jquery'                : [ '/assets/lib/jquery/jquery'],
        'domReady'              : [ '/assets/lib/requirejs-domready/domReady'],
        'requirejs'             : [ '/assets/lib/requirejs/require' ],
        'marked'                : [ '/assets/lib/marked/marked' ],
        'angular'               : [ '/assets/lib/angularjs/angular' ],
        'ngRoute'               : [ '/assets/lib/angularjs/angular-route'],
        'ng.ui'                 : [ '/assets/lib/angular-ui/angular-ui'],
        'ng.DragAndDrop'        : [ '/assets/lib/angular-dragdrop/draganddrop' ],
        'ng.MultiSelect'        : [ '/assets/lib/angular-multi-select/angular-multi-select'],
        'ng.ui.bootstrap'       : [ '/assets/lib/angular-ui-bootstrap/ui-bootstrap'] ,
        'ng.ui.bootstrap.tpls'  : [ '/assets/lib/angular-ui-bootstrap/ui-bootstrap-tpls'] ,
        'ng.ui.calendar'        : [ '/assets/lib/angular-ui-calendar/calendar'],
        'ng.ui.router'          : [ '/assets/lib/angular-ui-router/angular-ui-router'],
        'ng.ui.Utils'           : [ '/assets/lib/angular-ui-utils/ui-utils'],
        'jqueryUI'              : [ '/assets/lib/jquery-ui/ui/jquery-ui'],
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
    },

    /** This is the really important part. RequireJS reads this file first because it is the only javascript file
     * referenced from the `<script> ` tag in plainPage.scala.html. Instead of risking script load ordering issues, we
     * do not put the ng-app directive in the HTML file. Instead we tell requireJs that it depends on
     * bootstrap-angular.js which it will load as part of processing this configuration. That javascript is what
     * bootstrap's AngularJS, not the ng-app directive.
     */
    deps: ['/assets/javascripts/bootstrap-angular.js']
});
