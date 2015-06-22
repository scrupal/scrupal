/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

package scrupal.test

import scrupal.api.types.{BundleType, StringType}

import scrupal.api._
import scrupal.api.types
import scrupal.core.nodes
import scrupal.core.sites.NodeSite
import scrupal.utils.OSSLicense

/** Created by reidspencer on 11/10/14.
  */
case class ScenarioGenerator(dbName : String, sites : Int = 1, apps : Int = 1, mods : Int = 1,
  ents : Int = 1, nods : Int = 1, flds : Int = 1, ftrs : Int = 1)(implicit val scrupal: Scrupal) {

  def genType(id : Int) : Type[_] = {
    val name = s"Type-$id"
    StringType(Symbol(name), name, ".*".r, 128)
  }

  val AnyString = StringType('AnyStr, "Any Str", ".*".r, 1024)

  def genEntity(id : Int) : Entity = {
    val fields = for (i ← 1 to flds) yield {
      s"Field-$i" → AnyString
    }
    val ty_name = s"FieldsForEntity-$id"
    val ty = types.BundleType(Symbol(ty_name), ty_name, fields.toMap)
    FakeEntity(s"Entity-$id", ty)
  }

  def genNode(id : Int) : Node = {
    val name = s"Node-$id"
    nodes.MessageNode(name, "text-success", s"This is node $name")
  }

  def genFeature(id : Int, mod : Module) : Feature = {
    val name = s"Feature-$id"
    Feature(Symbol(name), name, Some(mod))
  }

  case class ScenarioModule(override val id : Symbol)(implicit val scrupal : Scrupal) extends AbstractFakeModule(id, dbName) {
    override val description = id.name
    def settingsTypes : BundleType = BundleType.Empty

    override def features : Seq[Feature] = {
      for (i ← 1 to ftrs) yield { genFeature(i, this) }
    }

    /** The core types that Scrupal provides to all modules */
    override def types : Seq[Type[_]] = {
      for (i ← 1 to nods) yield { genType(i) }
    }

    override def entities : Seq[Entity] = {
      for (i ← 1 to ents) yield { genEntity(i) }
    }

    override def nodes : Seq[Node] = {
      for (i ← 1 to nods) yield { genNode(i) }
    }
  }

  def genModule(id : Int) : Module = {
    val name = s"Module-$id"
    ScenarioModule(Symbol(name))
  }

  def genApplication(id : Int, mods : Int, ents : Int, instances : Int, nodes : Int) : Application = {
    val modules : Seq[Module] = for (i ← 1 to mods) yield {
      genModule(i)
    }
    val name = s"Application-$id"
    BasicApplication(Symbol(name), name, name, name, OSSLicense.ApacheV2, name)
  }

  def genSite(id : Int, apps : Int, mods : Int, ents : Int, instances : Int, nodes : Int) = {
    val applications : Seq[Application] = for (i ← 1 to apps) yield {
      genApplication(i, mods, ents, instances, nodes)
    }
    val name = s"Site-$id"
    NodeSite(Symbol(name), name, name, "localhost".r, Node.Empty)
  }
}
