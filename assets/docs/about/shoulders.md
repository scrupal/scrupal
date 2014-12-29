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
# Standing On The Shoulders Of Giants
Scrupal would not be possible without the free/libre/open source software community. Any recognition it
achieves clearly needs to acknowledge the technologies upon which it is built, and the dedication and brilliance
of their inventors. This page is all about giving such recognition and making sure we honor their software license
agreement through inclusion of their licenses here.
</div>
<div role="tabpanel">
<ul class="nav nav-pills" role="tablist">
<li role="presentation" class="active">
  <a href="#Java" aria-controls="Java" role="tab" data-toggle="pill">Java</a></li>
<li role="presentation">
  <a href="#Scala" aria-controls="Scala" role="tab" data-toggle="pill">Scala</a></li>
<li role="presentation">
  <a href="#Akka" aria-controls="Akka" role="tab" data-toggle="pill">Akka</a></li>
<li role="presentation">
  <a href="#MongoDB" aria-controls="MongoDB" role="tab" data-toggle="pill">MongoDB</a></li>
<li role="presentation">
  <a href="#AngularJS" aria-controls="AngularJS" role="tab" data-toggle="pill">AngularJS</a></li>
<li role="presentation">
  <a href="#Bootstrap" aria-controls="Bootstrap" role="tab" data-toggle="pill">Bootstrap</a></li>
<li role="presentation">
  <a href="#PBKDF2" aria-controls="PBKDF2" role="tab" data-toggle="pill">PBKDF2</a></li>
<li role="presentation">
  <a href="#BCrypt" aria-controls="BCrypt" role="tab" data-toggle="pill">BCrypt</a></li>
<li role="presentation">
  <a href="#SCrypt" aria-controls="SCrypt" role="tab" data-toggle="pill">SCrypt</a></li>
<li role="presentation">
  <a href="#Specs2" aria-controls="Specs2" role="tab" data-toggle="pill">Specs2</a></li>
<li role="presentation">
  <a href="#OthersToDo" aria-controls="OthersToDo" role="tab" data-toggle="pill">Others</a><li>
</ul>
<div class="tab-content">
<div class="tab-pane active" id="Java">
<h3>The Java Virtual Machine and Java Programming Language</h3>
<p>Scrupal runs on top of the long trusted, production competent Java Virtual Machine. Running application software
this way has made sense for numerous organizations and will continue to do so for many years to come. The depth of
resources, wealth of libraries, and deployment flexibility in the JVM eco-system bring levels of competence and
capability Scrupal could not achieve otherwise. Because Scrupal does not re-distribute the JVM, we do not have a
license restriction. However, if you plan to use the "Commerical Features" provided by the JVM via Scrupal then you
should be aware of this clause of the Oracle BCL:</p>
<pre style="font-size: 8pt">
Use of the Commercial Features for any commercial or production purpose requires a separate license from Oracle.
“Commercial Features” means those features identified Table 1-1 (Commercial Features In Java SE Product Editions) of
the Java SE documentation accessible at
</pre>
<div><a href="http://www.oracle.com/technetwork/java/javase/documentation/index.html">Java Documentation</a></div>
<div><a href="http://www.oracle.com/technetwork/java/javase/terms/license/index.html">Oracle Binary Code License</a>
</div>
</div>

<div role="tabpanel" class="tab-pane" id="Scala">
<h3>Scala Programming Language</h3>
<p>Almost all of Scrupal is written in Scala. The initial letters of Scrupal's name are attributed to Scala or at least
it aims to have the same concern: scalability. We tip our hats to all those who have made Scala into such a beautiful
language to work with. The Scala Programming Language requires the following copyright notices to be reproduced:</p>
<pre style="font-size: 8pt">
Copyright (c) 2002-2013 EPFL
Copyright (c) 2011-2013 Typesafe, Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the EPFL nor the names of its contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
<a href="http://www.scala-lang.org/license.html">Scala License</a>
</div>

<div role="tabpanel" class="tab-pane" id="Akka">
<h3>Akka</h3>
<p>Akka provides the data flow engine in Scrupal. It handles the HTTP interface, Actor model, streams, inter-process
messaging, and other facilities for fault-tolerant, non-blocking, asynchronous operations.</p>
<pre style="font-size: 8pt">
This software is licensed under the Apache 2 license, quoted below.

Copyright 2009-2014 Typesafe Inc. <http://www.typesafe.com>

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
</pre>
<a href="http://opensource.org/licenses/Apache-2.0">License</a>
</div>

<div role="tabpanel" class="tab-pane" id="MongoDB">
<h3>Mongo DB</h3>
<pre style="font-size: 8pt">
GNU AFFERO GENERAL PUBLIC LICENSE

Version 3, 19 November 2007

Copyright © 2007 Free Software Foundation, Inc. <http://fsf.org/>
</pre>
<a href="http://opensource.org/licenses/AGPL-3.0">GNU Affero General Public License</a>
<a href="http://www.mongodb.org/about/licensing/">About MongoDB Licensing</a>
</div>

<div role="tabpanel" class="tab-pane" id="AngularJS">
<h3>AngularJS</h3>
<p>AngularJS provides the foundation for One Page Applications in Scrupal.</p>
<pre style="font-size: 8pt">
The MIT License

Copyright (c) 2010-2014 Google, Inc. http://angularjs.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
</pre>
<a href="http://opensource.org/licenses/MIT">MIT License</a>
</div>

<div role="tabpanel" class="tab-pane" id="Bootstrap">
<h3>Twitter Bootstrap</h3>
<pre style="font-size: 8pt">
The MIT License (MIT)

Copyright (c) 2011-2014 Twitter, Inc

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
</pre>
<a href="http://opensource.org/licenses/MIT">MIT License</a>
</div>

<div role="tabpanel" class="tab-pane" id="PBKDF2">
<h3>PBKDF2</h3>
The PBKDF2 Scala library uses the following license:
<pre style="font-size: 8pt">
Copyright 2013 Nicolas Rémond (@nremond)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
</pre>
<a href="http://www.apache.org/licenses/LICENSE-2.0.html">Apache 2 License</a>
</div>

<div role="tabpanel" class="tab-pane" id="BCrypt">
<h3>BCrypt</h3>
The BCrypt library requires the following notice to be reproduced:
<pre style="font-size: 8pt">
Copyright (c) 2002 Johnny Shelley All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that t\he
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the author nor any contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>
</div>
<div role="tabpanel" class="tab-pane" id="SCrypt">
<h3>Scrypt</h3>
The Scrypt library uses Attribution-NonCommercial-NoDerivs 3.0 United States (CC BY-NC-ND 3.0 US) and the copyright
notice below is sufficient attribution.
<pre style="font-size: 8pt">
Copyright (c) 2013 DCIT, a.s. http://www.dcit.cz / Karel Miko
</pre>
<h href="http://creativecommons.org/licenses/by-nc-nd/3.0/us/">C BY-NC-ND 3.0 US</a>
</div>

<div role="tabpanel" class="tab-pane" id="Specs2">
<h3>Specs2</h3>
<p>TBD</p>
</div>

<div rle="tabpanel" class="tab-pane" id="OthersToDo">
<h3>Others - To Do</h3>
<p>There are many other pieces of software we depend upon, which have not been broken out separately ... yet!</p>
<pre style="font-size:8pt">
addSbtPlugin("com.scalatags" %% "scalatags" % "0.4.3-M1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.7.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.5.2")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.1")
addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.6.0")
val play_iteratees          = "com.typesafe.play"   %% "play-iteratees"         % playV
val play_json               = "com.typesafe.play"   %% "play-json"              % playV
val play_ws                 = "com.typesafe.play"   %% "play-ws"                % playV
val scala_arm               = "com.jsuereth"        %% "scala-arm"              % "1.4"
val livestream_scredis      = "com.livestream"      %% "scredis"                % "2.0.5"
val requirejs               = "org.webjars"         % "requirejs"               % "2.1.15"
val osgi_core               = "org.osgi"            % "org.osgi.core"           % "6.0.0"
val grizzled_slf4j          = "org.clapper"         %% "grizzled-slf4j"         % "1.0.2"
val mango                   = "org.feijoas"         %% "mango"                  % "0.11-SNAPSHOT"
val joda_time               = "joda-time"           %  "joda-time"              % "2.5"
val joda_convert            = "org.joda"            % "joda-convert"            % "1.2"
val config                  =  "com.typesafe"       %  "config"                 % "1.2.1"
val logback_classic         = "ch.qos.logback"      %  "logback-classic"        % "1.1.2"      % "test"
val specs2                  = "org.specs2"          %% "specs2-core"            % "2.3.11"     % "test"
val commons_io              = "commons-io"          %  "commons-io"             % "2.4"        % "test"
val commons_lang3           = "org.apache.commons"  % "commons-lang3"           % "3.3.2"

</pre>
</div>
</div>
