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

package scrupal.db

import scala.slick.lifted.DDL
import scrupal.api._
import java.sql.Clob
import play.api.libs.json.{Json, JsObject}
import scrupal.utils.Version

/** I.T.E.M.S Component
  * We over used the term "Core" so this group of tables got a memorable acronym. This component defines the five
  * central tables in Scrupal: Instances, Types, Entities, Modules, Sites. Logically,
  * a site is configured with various modules. Modules define Entities and Types. Types give structure to Instances.
  * Entities give functionality to Instance. Users create Instance from the allowed types. This is the basis of all
  * information storage in Scrupal.
  */
trait ITEMSComponent extends Component {

  import sketch.profile.simple._

  // Get the TypeMapper for DateTime
  import CommonTypeMappers._

  // Need mappings for Version -> ID, Module -> ID, ENtitTpype -> ID

  implicit  val JsObjectMapper = MappedTypeMapper.base[JsObject,Clob] (
    { j => { val result = session.conn.createClob(); result.setString(1, Json.stringify(j)); result } },
    { c => Json.parse( c.getSubString(1, c.length().toInt)).asInstanceOf[JsObject] }
  )

  implicit val VersionMapper = MappedTypeMapper.base[Version,String] (
    { v:Version => v.toString },
    { s:String => {
      val parts= s.split("\\.")
      assert(parts.size==3)
      Version(parts(0).toInt, parts(1).toInt, parts(2).toInt)
    }}
  )

  object Instances extends ScrupalTable[Instance]("instances") with NumericThingTable[Instance] {
    def entityId = column[TypeIdentifier](nm("entityId"))
    def entityId_fkey = foreignKey(fkn("entityId"), entityId, Entities)(_.id)
    def payload = column[JsObject](nm("payload"), O.NotNull)
    def forInsert = name ~ description ~ entityId ~ payload
    def * = forInsert ~ modified.? ~ created.? ~ id.?  <> (Instance.tupled, Instance.unapply _)
  }

  object Types extends ScrupalTable[EssentialType]("types")
                       with SymbolicTable[EssentialType]
                       with DescribableTable[EssentialType] {
    def moduleId = column[ModuleIdentifier](nm("moduleId"))
    def moduleId_fKey = foreignKey(fkn(Modules.tableName), moduleId, Modules)(_.id)
    def * = id ~ description ~ moduleId <> (EssentialType.tupled, EssentialType.unapply _)
  }

  object Entities extends ScrupalTable[EssentialEntity]("entities")
                          with SymbolicTable[EssentialEntity]
                          with DescribableTable[EssentialEntity] {
    def typeId = column[TypeIdentifier](nm("typeId"))
    def typeId_fkey = foreignKey(fkn("typeId"), typeId, Types)(_.id)
    def * = id ~ description ~ typeId <> (EssentialEntity.tupled, EssentialEntity.unapply _ )
  }

  object Modules extends ScrupalTable[EssentialModule]("modules")
                         with SymbolicTable[EssentialModule]
                         with DescribableTable[EssentialModule] {
    def version = column[Version](nm("version"))
    def obsoletes = column[Version](nm("obsoletes"))
    def enabled = column[Boolean](nm("enabled"))
    def * = id ~ description ~ version ~ obsoletes ~ enabled  <>
      (EssentialModule.tupled , EssentialModule.unapply _ )
  }

  object Sites extends ScrupalTable[EssentialSite]("sites") with SymbolicEnablableThingTable[EssentialSite] {
    def listenPort = column[Short](nm("listenPort"))
    def listenPort_index = index(idx("listenPort"), listenPort, unique=true)
    def urlDomain = column[String](nm("urlDomain"))
    def urlPort = column[Short](nm("urlPort"))
    def urlHttps = column[Boolean](nm("urlHttps"))
    def * = id ~ description ~ listenPort ~ urlDomain ~ urlPort ~ urlHttps ~ enabled ~ modified.? ~ created.? <>
      (EssentialSite.tupled, EssentialSite.unapply _)

  }

  def coreDDL : DDL = Instances.ddl ++ Types.ddl ++ Entities.ddl ++  Modules.ddl ++ Sites.ddl
}
