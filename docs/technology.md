## Technology
Scrupal is an integration of some of the latest technologies to ensure that it remains technically relevant for many
decades to come. Below is the rational for choosing the technologies that comprise Scrupal.

### Scala
When picking a language system there were many choices but several important criteria led to Scala:
* Run everywhere - Scala runs on the JVM which means it doesn't need to be ported to each operating system.
* Proven platform - The Java VM has been stable for over a decade and performs very well.
* Rich Library - Both Java and Scala have deep, rich libraries full of functionality that can be utilized.
* Functional - Composition of scalable asynchronous systems is more efficient
* Compact - The syntax should be terse and the compiler should DTRT&tm; with much inference in context.
* Object Oriented - This programming paradigm has proven useful for the last 20+ years

### Play Framework
We did not want to have to write the basics of our web application from scratch. To choose a framework we reviewed many
of the leading JVM based systems but only two had the characteristics we were looking for:
* 100% Asynchronous web server
* Flexible router that supports REST apis
* MVC model throughout
* Actor model supported
Erlang was considered because of its direct support for the Actor model, and its strong reputation for building
rock solid server side applications. However, Erlang is merely a language and not a web application framework. It is
consequently not able to provide the other facilities that Play does. Liftweb was another strong contender but we chose
Play because of the larger community around Play, the better documentation available, and we also found Play to be
more conceptually elegant and simpler than Lift. Additionally, Play utilizing and integrating SBT is a real win as it
permits us to do interesting things with the build and deployment side of Scrupal.

### MongoDB & ReactiveMongo

### Ember.js

### Specs2

### SecureSocial

### Deadbolt

- - -
<sub><sup>&copy; Copyright 2013, Reid Spencer. All Rights Reserved.</sup></sub>
