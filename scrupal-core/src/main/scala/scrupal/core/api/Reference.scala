/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.api

import reactivemongo.bson._
import scrupal.utils.{IdentifiedWithRegistry, Identifiable}

import BSONHandlers._

/** Reference A Memory Object From The Database
  *
  * Many of the objects used in Scrupal are not stored in the database. Essentially those things coming from a Module
  * are just retained in memory: Module, Feature, Entity, Type, Handler, etc. However, we need to reference these
  * things from objects that are stored in the database. We do not want to be loading the low level memory objects
  * all the time but would just like to "find" them. That's what the Registry/Registrable traits are for. We can
  * register long-lived immutable objects in a registry and find them later, by name. But, there is a registry per
  * object type, by design. So, we need a way to capture a reference to a named object in one of the registries.
  *
  * That's what this class is for.
  */
class Reference[+T <: IdentifiedWithRegistry ](val id: Symbol, val registry: String) extends ( () ⇒ Option[T] ) {
  def as[C]() = this.apply().map { x ⇒ x.asInstanceOf[C] }
  def apply() : Option[T] = {
    registry match {
      case Type.registryName    ⇒ Some(Type.lookup(id).asInstanceOf[T])
      case Feature.registryName ⇒ Some(Feature.lookup(id).asInstanceOf[T])
      case Entity.registryName  ⇒ Some(Entity.lookup(id).asInstanceOf[T])
      case Module.registryName  ⇒ Some(Module.lookup(id).asInstanceOf[T])
      case Template.registryName ⇒ Some(Template.lookup(id).asInstanceOf[T])
      case _ ⇒ None
    }
  }
}

case class TypeReference(override val id: Symbol) extends Reference[Type](id,Type.registryName) {
  def this(t: Type) = this(t.id)
  override def apply() : Option[Type] = Type.lookup(id)
}

object TypeReference {
  implicit val TypeReferenceHandler = Macros.handler[TypeReference]
}

case class FeatureReference(override val id: Symbol) extends Reference[Feature](id,Feature.registryName) {
  def this(f: Feature) = this(f.id)
  override def apply(): Option[Feature] = Feature.lookup(id)
}

object FeatureReference {
  implicit val FeatureReferenceHandler = Macros.handler[FeatureReference]
}

case class EntityReference(override val id: Symbol) extends Reference[Entity](id,Entity.registryName) {
  def this(e: Entity) = this(e.id)
  override def apply(): Option[Entity] = Entity.lookup(id)
}

object EntityReference {
  implicit val EntityReferenceHandler = Macros.handler[EntityReference]
}

case class ModuleReference(override val id: Symbol) extends Reference[Module](id,Module.registryName) {
  def this(m: Module) = this(m.id)
  override def apply(): Option[Module] = Module.lookup(id)
}

object ModuleReference {
  implicit val ModuleReferenceHandler = Macros.handler[ModuleReference]
}

object Reference {
  implicit val ReferenceHandler : BSONHandler[BSONDocument,Reference[IdentifiedWithRegistry]] =
    new BSONHandler[BSONDocument, Reference[IdentifiedWithRegistry]] {
    def write(f: Reference[IdentifiedWithRegistry]): BSONDocument = BSONDocument(
      "id" → BSONString(f.id.name ), "registry" → BSONString(f.registry)
    )
    def read(bson: BSONDocument): Reference[IdentifiedWithRegistry] =
      new Reference(Symbol(bson.getAs[String]("id").get), bson.getAs[String]("registry").get)
  }

  def to[T <: IdentifiedWithRegistry](o: T) : Reference[IdentifiedWithRegistry] = {
    o match {
      case t: Type ⇒ new TypeReference(t)
      case f: Feature ⇒ new FeatureReference(f)
      case e: Entity ⇒ new EntityReference(e)
      case m: Module ⇒ new ModuleReference(m)
      case _ ⇒ new Reference(o.id,o.registryName)
    }
  }

  // TODO: Make it possible to add other types of things that can be referenced (only if actually needed some day)
}
