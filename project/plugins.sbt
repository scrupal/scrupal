resolvers ++=  Seq( Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots") )

addSbtPlugin("org.scrupal" % "scrupal-sbt" % "0.2.0-SNAPSHOT" )

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.2.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.0")

// addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.2")

