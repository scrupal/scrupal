package scrupal.core.api

import com.typesafe.config.Config
import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson.{Macros, BSONObjectID}
import scrupal.db.{Storable, DataAccessObject}

/** Configuration Data */
case class ConfigData(
  _id : BSONObjectID,
  config : Config
) extends Storable[BSONObjectID] {

  def validate(required: Map[String,Type], optional: Map[String,Type]) = ???
}

object ConfigData {

  case class ConfigDataDao(db: DefaultDB) extends DataAccessObject[ConfigData,BSONObjectID] {
    final def collectionName = "configs"
    implicit val reader : DataAccessObject[ConfigData,BSONObjectID]#Reader = Macros.reader[ConfigData]
    implicit val writer : DataAccessObject[ConfigData,BSONObjectID]#Writer = Macros.writer[ConfigData]
    implicit val converter = (id: BSONObjectID) => id
    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("module" -> IndexType.Ascending), name = Some("Module"))
    )
  }
}
