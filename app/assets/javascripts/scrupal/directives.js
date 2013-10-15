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

/* Scrupal Directives */

define(['angular', 'marked'], function(angular, marked) {

    /** Create the sub-module of scrupal named directives. We define the scrupal directives here */
    var mod = angular.module('scrupal.directives', [])

    /**
     * Create a "marked" element that contains markdown content which is automatically converted, client side, into
     * HTML via the marked.js module.
     */

    mod.directive('marked', function($compile) {
        return {
            restrict: 'EAC',
            // priority: 99,
            link: function (scope, element, attrs) {
                if (attrs.marked) {
                    scope.$watch(attrs.marked, function (newVal) {
                        var html = newVal ? "<div class=\"marked\">" + marked(newVal) + "</div>" : '';
                        element.replaceWith(html);
                    });
                } else {
                    var html = "<div class=\"marked\">" + marked(element.text()) + "</div>";
                    element.replaceWith(html);
                }
            }
        }
    });

    return mod;
})
