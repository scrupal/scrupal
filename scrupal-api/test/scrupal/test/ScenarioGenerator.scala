/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.test

import scrupal.api.HtmlHelpers._
import scrupal.api._

/**
 * Created by reidspencer on 11/10/14.
 */
case class ScenarioGenerator(dbName: String, sites: Int = 1, apps: Int = 1, mods: Int = 1,
                        ents: Int = 1, nods: Int = 1, flds: Int = 1, ftrs: Int = 1) {

  def genType(id: Int) : Type = {
    val name = s"Type-$id"
    StringType(Symbol(name),name,".*".r, 128)
  }

  val AnyString = StringType('AnyStr, "Any Str", ".*".r, 1024)

  def genEntity(id: Int) : Entity = {
    val fields = for (i ← 1 to flds ) yield {
      s"Field-$i" → AnyString
    }
    val ty_name = s"FieldsForEntity-$id"
    val ty = BundleType(Symbol(ty_name),ty_name,fields.toMap)
    FakeEntity(s"Entity-$id",ty)
  }

  def genNode(id: Int) : Node = {
    val name = s"Node-$id"
    MessageNode(name,"text-success", s"This is node $name".toHtml)
  }

  def genFeature(id: Int, mod: Module) : Feature = {
    val name = s"Feature-$id"
    Feature(Symbol(name), name, Some(mod))
  }

  case class ScenarioModule(override val id: Symbol) extends AbstractFakeModule(id,dbName) {
    override val description = id.name

    override def features : Seq[Feature] = {
      for (i ← 1 to ftrs) yield { genFeature(i, this) }
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
    BasicApplication(Symbol(name), name, name)
  }

  def genSite(id: Int, apps: Int, mods: Int, ents: Int, instances: Int, nodes: Int) = {
    val applications :Seq[Application] = for ( i <- 1 to apps ) yield {
      genApplication(i, mods, ents, instances, nodes)
    }
    val name = s"Site-$id"
    NodeSite(Symbol(name), name, name, "localhost", Node.Empty)
  }
}
