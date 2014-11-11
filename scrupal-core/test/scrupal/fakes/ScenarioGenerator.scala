package scrupal.fakes

import play.twirl.api.Html
import scrupal.core._
import scrupal.core.api._

/**
 * Created by reidspencer on 11/10/14.
 */
case class ScenarioGenerator(dbName: String, sites: Int = 1, apps: Int = 1, mods: Int = 1,
                        ents: Int = 1, nods: Int = 1, flds: Int = 1, ftrs: Int = 1) {

  def genType(id: Int) : Type = {
    val name = s"Type-$id"
    StringType(Symbol(name),name,".*".r, 128)
  }

  def genEntity(id: Int) : Entity = {
    val fields = for (i ← 1 to flds ) yield {
      s"Field-$i" → AnyString_t
    }
    val ty_name = s"FieldsForEntity-$id"
    val ty = BundleType(Symbol(ty_name),ty_name,fields.toMap)
    FakeEntity(s"Entity-$id",ty)
  }

  def genNode(id: Int) : Node = {
    val name = s"Node-$id"
    MessageNode(Symbol(name),name,"text-success", Html(s"This is node $name"))
  }

  def genFeature(id: Int) : Feature = {
    val name = s"Feature-$id"
    Feature(Symbol(name), name)
  }

  case class ScenarioModule(override val id: Symbol) extends AbstractFakeModule(id,dbName) {
    override val description = id.name

    override def features : Seq[Feature] = {
      for (i ← 1 to ftrs) yield { genFeature(i) }
    }

    /** The core types that Scrupal provides to all modules */
    override def types : Seq[Type] = {
      for (i ← 1 to nods) yield {genType(i) }
    }

    override def entities : Seq[Entity] = {
      for (i ← 1 to ents) yield { genEntity(i) }
    }

    override def nodes : Seq[Node] = {
      for (i ← 1 to nods) yield { genNode(i) }
    }
  }

  def genModule(id: Int) : Module = {
    val name = s"Module-$id"
    ScenarioModule(Symbol(name))
  }

  def genApplication(id: Int, mods: Int, ents: Int, instances: Int, nodes: Int) : Application = {
    val modules : Seq[Module] = for (i ← 1 to mods ) yield {
      genModule(i)
    }
    val name = s"Application-$id"
    BasicApplication(Symbol(name), name, name, name, modules)
  }

  def genSite(id: Int, apps: Int, mods: Int, ents: Int, instances: Int, nodes: Int) = {
    val applications :Seq[Application] = for ( i <- 1 to apps ) yield {
      genApplication(i, mods, ents, instances, nodes)
    }
    val name = s"Site-$id"
    BasicSite(Symbol(name), name, name, "localhost", Node.Empty, applications)
  }
}
