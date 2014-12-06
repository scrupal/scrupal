<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright © 2014 Reactific Software LLC                                                                           ~
  ~                                                                                                                   ~
  ~ This file is part of Scrupal, an Opinionated Web Application Framework.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is free software: you can redistribute it and/or modify it under the terms                                ~
  ~ of the GNU General Public License as published by the Free Software Foundation,                                   ~
  ~ either version 3 of the License, or (at your option) any later version.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                              ~
  ~ without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         ~
  ~ See the GNU General Public License for more details.                                                              ~
  ~                                                                                                                   ~
  ~ You should have received a copy of the GNU General Public License along with Scrupal.                             ~
  ~ If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                        ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->
# Development Policies

## Semantic Versioning
Scrupal uses [Semantic Versioning 2.0](http://semver.org/spec/v2.0.0.html) for its versioning policy. This means that,
given a version number MAJOR.MINOR.PATCH, increment the:

+ MAJOR version when you make incompatible API changes,
+ MINOR version when you add functionality in a backwards-compatible manner, and
+ PATCH version when you make backwards-compatible bug fixes.

Additionally, pre-release versions will have a _-SNAPSHOT_ suffix added to them for compatibility with Ivy 2 and
Maven repositories that support frequently changing versions.

There will be no back-porting of fixes to prior release versions before 1.0

## Patch & Pull Request Standards

+ Follow applicable portions of [Play's guidelines](http://www.playframework.com/documentation/2.2.x/Guidelines)
+ No Java code, Scala only.
+ Code review is our first line of quality assurance. Do not be offended if your submission is heavily critiqued.
Instead, learn from the review, or point out where you believe the reviewer may have made a mistake. The point is to
have a conversation about the code until multiple people are happy that the patch is the RightThing(tm)

## GIT Workflow

+ Read [this](https://sandofsky.com/blog/git-workflow.html) and follow it. ;)
