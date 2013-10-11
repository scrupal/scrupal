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

// Start the main app logic.
requirejs([
    'require',
    'angular',
    'marked'
], function( require, angular, marked) {

    /**
     * Declare the scrupal module. This is fundamental to Scrupal applications and this module provides all the basic
     * elements and capabilities that any Scrupal application needs. What it doesn't do is provide any particular
     * application level constructs, just facilities applications can utilize.
     */
    var scrupal = angular.module("scrupal", []);

    /**
     * Create a "marked" element that contains markdown content which is automatically converted, client side, into
     * HTML via the marked.js module.
     */
    scrupal.directive('marked', function($compile) {
        return {
            restrict: 'E',
            link: function(scope, element, attrs) {
                var htmlText = "<div class=\"marked\">" + marked(element.text()) + "</div>";
                var e = $compile(htmlText)(scope);
                element.replaceWith(e);
            }
        }
    });
});
