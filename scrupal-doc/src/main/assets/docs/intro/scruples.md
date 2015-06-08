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

## Scruples
The word scruple means *a moral or ethical consideration or standard that acts as a restraining force or inhibits
certain actions*. In the context of the Scrupal software, our "moral or ethical consideration or standard" has to do
with fundamental technical and sociological principles of computing systems and the people that use them. Those
principles have restrained and inhibited the Scrupal developers from the outset to ensure that
Scrupal has scruples. Here's what they are.

### Testability
When we write code, we write the test plan first. That is, we use Test Driven Design. By using specs2 this serves as
a way to communicate about future features as well since it is so easily read and written. We use the following
scruples:
* Every code module has a competent test suite written for it that covers at least 80% of the code.
* The master branch always passes all tests, no exceptions.
* Code changes that cause test suites to fail will be reverted with impunity.
* Test code is written before the code we develop.
* Attempts to subvert the above will cause a developer to lose his commit privileges.


### Scalability

### Security

### Flexibility
A data model that is highly extensible so it can adapt to unforeseen uses.

### Simplicity - You can set up a web site without ever writing a line of code.
