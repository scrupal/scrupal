# Scrupal API Documentation

This is the _developer_ documentation for the scrupal-api module of Scrupal.

The API module is where all the essential classes and traits are defined. It is also where the processing engine of
Scrupal resides. The core abstractions in Scrupal form an acronym, MANIFESTO, which stands for:

- **M: Module:** A container of functionality that defines Applications, Nodes, Entities, and Types

- **A: Application:** A URL context and a set of enabled modules, entities and nodes

- **N: Node:** A content generation function

- **I: Instance:** An instance of an entity (essentially a document)

- **F: Facet:** Something to add on to an instance's main payload

- **E: Entity:** A type of instance with definitions for the actions that can be performed on it

- **S: Site:** Site management data and a set of applications enabled for it.

- **T: Type:** A fundamental data type used for validating BSONValue structured information (Instances and Node results)

- **O: Other:** Reserved for future use :)

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
