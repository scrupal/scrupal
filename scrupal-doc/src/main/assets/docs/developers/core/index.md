# Scrupal Core Project

The scrupal-core project is where all the essential classes and traits for Scrupal are defined. It is also where the
processing engine of Scrupal resides. The core abstractions in Scrupal form an acronym, MANIFESTO, which stands for:

- **[M: Module](module.md):** A container of functionality that defines the other types of api objects.

- **[A: Action](action.md):** A functor that produces a Result; the primary object of behavior in Scrupal.

- **[N: Node](node.md):** A content generation function that converts a Context into a Result

- **[I: Instance](instance.md):** An instance of an entity (essentially a document)

- **[F: Facet](facet.md):** Something to add on to an instance's main payload; a mechanism of extension

- **[E: Entity](entity.md):** A type of instance with definitions the actions that can be performed on it

- **[S: Site](site.md):** A site definition as a collection of enabled capabilities.

- **[T: Type](type.md):** A fundamental data type used for validating BSONValues (Instances and Node results)

- **[O: Others](others.md):** Other objects of lesser importance.

The scrupal-core project contains the CoreModule class which encapsulates the MANIFEST items at the root of all
Scrupal applications.

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


