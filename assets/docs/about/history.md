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
# History
The initial idea for Scrupal started in about 2010 when Reid was working at Doyenz, Inc. and wishing for a better
platform on which Doyenz' software could be based. When Reid left Doyenz in 2012, he needed a modern publishing and
content management system for a new business. Having had experience with Drupal and WordPress many years earlier, he
started to implement his designs with those technologies. Unfortunately, neither technology was scalable enough for the
system he was designing so he began to find the modern technologies that would take advantage of multi-core systems and
provide an asynchronous non-blocking infrastructure.

In late 2012, Reid started working with Scala, Akka, Play, and a very early version of Slick. By the end of 2013, a
rudimentary 0.1 release of Scrupal was prepared based on these technologies. In 2014, the decision was made to ensure
that everything was non-blocking from database to client. As a result, MongoDB was chosen for the database instead of
relational databases fronted by Slick. This made the database access much more flexible and reduced the amount of code
needed for the database significantly.

Commensurate with the switch to MongoDB, it was decided that BSON was a competent and efficient data structure for all
inter-process communication and so BSON Documents became integrated throughout Scrupal's API. About the same time, late
in 2014, it was decided that Play had significant infrastructural overheads of which Scrupal only used a small fraction.
Since Play's designers had been kind enough to break out libraries for Twirl, sbt-web, and iteratee/enumerator, it made
sense to look for a high performance HTTP engine. At that time the Akka project was nearing completion of the
integration of Spray into akka as the akka-http module. The decision was made to use spray-can, spray-http and
spray-routing as an interim measure until akka-http was released.
