// Comment to get more information during initialization
logLevel := Level.Info

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype respository" at "https://oss.sonatype.org/content/repositories/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.0")

addSbtPlugin("com.github.mpeltonen" %% "sbt-idea" % "1.5.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-atmos" % "0.3.2")
