/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.models.db

import scala.util.matching.Regex
import scala.slick.lifted.DDL
import org.joda.time.DateTime
import scala.Enumeration


object EssentialDatumKinds extends Enumeration {
  type Type = Value
  val Boolean = Value
  val String = Value
  val Integer = Value
  val Real = Value
  val Timestamp = Value
  val Enum = Value
  val Entity = Value
  val List = Value
  val Map = Value
  val BLOB = Value
}

case class Enum(
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String
) extends Thing[Enum] {
  def forId(id: Long) = Enum(Some(id), created, label, description)
}

class DatumConstraint(
  val kind: EssentialDatumKinds.Type
)

case class StringConstraint(
  regex: Regex,
  maxLen: Int,
  override val kind: EssentialDatumKinds.Type = EssentialDatumKinds.String
) extends DatumConstraint(kind)

case class EnumConstraint(
  enumerators: Set[String],
  override val kind: EssentialDatumKinds.Type = EssentialDatumKinds.Enum
) extends DatumConstraint(kind)

case class IntegerConstraint(
  min: Long,
  max: Long,
  override val kind: EssentialDatumKinds.Type = EssentialDatumKinds.Integer
) extends DatumConstraint(kind)

case class DatumKind(
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  kind: EssentialDatumKinds.Type,
  mimeType : String,
  regex    : Option[Regex] = None,
  elemId   : Option[Long] = None, // element datumKind Id
  iMin     : Option[Long] = Some(Long.MinValue),
  iMax     : Option[Long] = Some(Long.MaxValue),
  rMin     : Option[Double] = Some(Double.MinValue),
  rMax     : Option[Double] = Some(Double.MaxValue),
  minLen   : Option[Int] = Some(0),
  maxLen   : Option[Int] = Some(Int.MaxValue),
  traitId  : Option[Long] = None // For entities, the trait it has
)  extends Thing[DatumKind] {
  def forId(id: Long) = DatumKind(Some(id), created, label, description, kind, mimeType, regex, elemId, iMin, iMax, rMin, rMax,
                                  minLen, maxLen, traitId)



  /**
   *
   * @param label
   * @param description
   * @return
   */
  def this(label: String, description: String) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.Boolean, "text/plain", None, None, None, None,
           None, None, None, None, None)
  /**
   * Constructor for Strings
   * @param label Name of the String datum kind
   * @param description Brief description of the String datum kind
   * @param regex Regular expression that validates the content of the String datum kind
   * @param minLen Minimum acceptable length for the String datum kind
   * @param maxLen Maximum acceptable length for the String datum kind
   * @return
   */
  def this(label: String, description: String, regex: Regex, minLen: Int = 0, maxLen : Int = Int.MaxValue) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.String, "text/plain", Some(regex), None,
           None, None, None, None, Some(minLen), Some(maxLen), None)

  /**
   * Constructor for Integers
   * @param label Name of the Integer datum kind
   * @param description Brief description of the Integer datum kind
   * @param minVal Minimum value for the Integer datum kind
   * @param maxVal Maximum value for the Integer datum kind
   * @return DatumKind
   */
  def this(label: String, description: String, minVal: Long, maxVal: Long) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.Integer, "text/plain", None, None,
           Some(minVal), Some(maxVal), None, None, None, None, None)

  /**
   * Constructor for Reals
   * @param label Name of the Real datum kind
   * @param description Brief description of the Real datum kind
   * @param minVal Minimum value for the Real datum kind
   * @param maxVal Maximum value for the Real datum kind
   * @return DatumKind
   */
  def this(label: String, description: String, minVal: Double, maxVal: Double) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.Real, "text/plain", None, None, None, None,
           Some(minVal), Some(maxVal), None, None, None)

  /**
   * Constructor for Enums
   * @param label
   * @param description
   * @param enum
   * @return
   */
  def this(label: String, description: String, enum: Enum) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.Enum, "text/plain", None, None, None, None,
           None, None, None, None, enum.id)

  /**
   * Constructor for Entities
   */
  def this(label: String, description: String, entity: Entity) =
    this(None, DateTime.now(), label, description, EssentialDatumKinds.Entity, "text/plain", None, None, None, None,
      None, None, None, None, entity.id)

  /**
   * Constructor for Lists
   * @param label
   * @param description
   * @param elem
   * @param maxLen
   * @return
   */
  def this(label: String, description: String, elem: DatumKind, maxLen : Integer) =
      this(None, DateTime.now(), label, description, EssentialDatumKinds.List, "text/plain", None, elem.id,
           None, None, None, None, None, Some(maxLen), None)

}


case class Trait (
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  override val modified: DateTime
) extends ModifiableThing[Trait] {
  def forId(id: Long) = Trait(Some(id), created, label, description, modified)
}

case class Field (
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  override val modified: DateTime,
  entity: Long,
  essentialKind: EssentialDatumKinds.Type,
  datumKind: Long
) extends ModifiableThing[Field] {
  def forId(id: Long) = Field(Some(id), created, label, description, modified, entity, essentialKind, datumKind)
}

case class Entity (
  override val id: Option[Long],
  override val created: DateTime,
  override val label: String,
  override val description: String,
  override val modified: DateTime = DateTime.now()
) extends ModifiableThing[Entity] {
  def forId(id: Long) = Entity(Some(id), created, label, description, modified)
}



/**
 * One line sentence description here.
 * Further description here.
 */
trait EntityComponent extends Component {

  import profile.simple._

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  object DatumKinds extends ThingTable[DatumKind]("datum_kinds") {
    def kind = column[EssentialDatumKinds.Type](tname + "_kind", O.NotNull)
    def mimeType = column[String](tname + "_mimeType", O.NotNull)
    def regex = column[Regex](tname + "_regex", O.Nullable)
    def elemId = column[Long](tname + "_elemId", O.Nullable)
    def elemId_fkey = foreignKey(tname + "_elemId_fkey", elemId, Entities)(_.id)
    def iMin = column[Long](tname + "_imin", O.Nullable)
    def iMax = column[Long](tname + "_imax", O.Nullable)
    def rMin = column[Double](tname + "_rmin", O.Nullable)
    def rMax = column[Double](tname + "_rmax", O.Nullable)
    def minLen = column[Int](tname + "_minLen", O.Nullable)
    def maxLen = column[Int](tname + "_maxLen", O.Nullable)
    def traitId = column[Long](tname + "_traitId", O.Nullable)
    def traitId_fkey = foreignKey(tname + "_traitId_fkey", traitId, Traits)(_.id)
    def * = id.? ~ created ~ label ~ description ~ kind ~ mimeType ~ regex.? ~ elemId.? ~
            iMin.? ~ iMax.? ~ rMin.? ~ rMax.? ~ minLen.? ~ maxLen.? ~ traitId.? <> (DatumKind.tupled, DatumKind.unapply _)
  }

  object Traits extends ModifiableThingTable[Trait]("traits") {
    def * = id.? ~ created ~ label ~ description ~ modified <> (Trait.tupled, Trait.unapply _)
  }

  object Entities extends ModifiableThingTable[Entity]("entities") {
    def * = id.? ~ created ~ label ~ description ~ modified <> (Entity.tupled, Entity.unapply _)
  }

  object Fields extends ModifiableThingTable[Field]("fields") {
    def entity = column[Long]("entity")
    def entity_fkey = foreignKey("fields", entity, Entities)(_.id)
    def essentialKind = column[EssentialDatumKinds.Type]("essentialKind")
    def datumKind = column[Long]("datumKind")
    def * = id.? ~ created ~ label ~ description ~ modified ~ entity ~ essentialKind ~ datumKind <> (Field.tupled, Field.unapply _)
  }

  object Fields_Traits extends ManyToManyTable[Field,Trait]("fieldsOfTraits", "fields","traits", Fields, Traits) {
    def fields(t: Trait)(implicit s: Session) : List[Field] = super.selectAssociatedA(t)
    def traits(field: Field)(implicit s: Session) : List[Trait] = super.selectAssociatedB(field)
  }

  object Entities_Traits extends ManyToManyTable[Entity,Trait]("traitsOfEntities", "entities", "traits", Entities, Traits) {
    def entities(t: Trait)(implicit s: Session) = super.selectAssociatedA(t)
    def traits(entity: Entity)(implicit s: Session) = super.selectAssociatedB(entity)
  }

  def entityDDL : DDL = DatumKinds.ddl ++ Traits.ddl ++
                      Entities.ddl ++ Fields.ddl ++ Fields_Traits.ddl ++ Entities_Traits.ddl

}
