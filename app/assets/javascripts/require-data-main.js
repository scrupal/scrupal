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
    baseUrl : "/assets/javascripts",

    /**
     * Shims are dependency settings for things that don't grok requirejs.
     */
    shim: {
        'jquery'    : { exports: "$" },
        'marked'    : { exports: "marked" },
        'angular'   : { exports: 'angular' },
        'jsrouts'   : { exports: "jsRoutes" },
        'domReady'  : { exports: "domReady" },
        "ui-router" : { exports: "uiRouter", deps : ['angular'] }
    },

    /** This is the really important part. RequireJS reads this file first because it is the only javascript file
     * referenced from the `<script> ` tag in main.scala.html. Instead of risking script load ordering issues, we
     * do not put the ng-app directive in the HTML file. Instead we tell requirJs that it depends on
     * bootstrap-angular.js which it will load as part of processing this configuration. That javascript is what
     * bootstrap's AngularJS, not the ng-app directive.
     */
    deps: ['./bootstrap-angular']
});

/** Create names for the various javascripts we use so we can depend on them more easily. */
define("jsroutes",      ["/assets/javascripts/jsroutes.js"],function(jsRoutes) { return jsRoutes; });
define("jquery",        ["webjars!jquery.js"],              function() { return $; });
define("angular",       ["webjars!angular.js"],             function() { return angular; });
define("angular-ui",    ["webjars!angular-ui.js"],          function(angularUI) { return angularUI; })
define("uiBootstrap",   ["webjars!angular-ui-bootstrap.js"],function(uiBootstrap) { return uiBootstrap; })
define("marked",        ["webjars!marked.js"],              function(marked) { return marked; });
define("domReady"       ["webjars!domReady.js"],            function(domReady) { return domReady; });
