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

requirejs.config({
    baseUrl : '/assets/javascripts',

    /*
    paths : {
        'angular' : 'webjars!angular',
        'bootstrap' : 'webjars!bootstrap',
        'jquery' : 'webjars!jquery',
        'domready' : 'webjars!domReady',
        'marked' : 'webjars!marked'
    },
    */

    /**
     * Shims are dependency settings for things that don't grok requirejs.
     */
    shim: {
        'angular': { exports: 'angular' }
    },

    // kick start application
    deps: ['./bootstrap-angular']
});


define("angular",   ["webjars!angular"],    function(angular)   { return angular; });
define("marked",    ["webjars!marked"],     function(marked)    { return marked; });
define("domReady"   ["webjars!domReady"],   function(domReady)  { return domReady; });
define("jsroutes",  ["/jsroutes"],          function(jsRoutes)  { return jsRoutes; });
