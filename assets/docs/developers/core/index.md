# Scrupal Core Project

The scrupal-core project contains the CoreModule class which provides a variety of features that extend the
scrupal-api project.


```scala
object CoreModule extends Module {
  def id = Core
  val description = "Scrupal's Core module for core, essential functionality."
  val version = Version(0,1,0)
  val obsoletes = Version(0,0,0)
  val moreDetailsURL = new URL("http://modules.scrupal.org/doc/" + label)
  val author : String = "Reid Spencer"
  val copyright : String = "(C) 2014 Reactific Software LLC. All Rights Reserved"
  val license = OSSLicense.GPLv3
}
```
