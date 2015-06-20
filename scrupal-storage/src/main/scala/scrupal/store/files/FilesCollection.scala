package scrupal.store.files

import java.io.{FileInputStream, FileOutputStream, File}
import java.nio.file.{Path, Files}

import com.esotericsoftware.kryo.io.{Input, Output}
import scrupal.storage.api._
import scrupal.storage.impl.{CommonCollection, KryoFormatter, KryoFormat}
import scrupal.utils.ScrupalComponent

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/** A Collection Stored In Memory */
case class FilesCollection[S <: Storable] private[files] (
  schema : FilesSchema,
  info : FilesCollectionInfo
) extends CommonCollection[S] with FilesInfo[FilesCollectionInfo] {

  final val name : String = info.name

  final val kind: String = "Collection"

  private[files] final val dir = new File(schema.dir, name)

  private[files] final val fileName = FilesStorageInfo.collection_info_file_name

  type SF = KryoFormat
  implicit val formatter: StorageFormatter[SF, S] = KryoFormatter.StorableFormatter.asInstanceOf[StorageFormatter[SF,S]]

  def size: ID = {
    Files.walk(dir.toPath).count()
  }

  def drop(implicit ec: ExecutionContext): Future[WriteResult] = Future {
    FilesStorageUtils.recursivelyDeleteDirectory(dir)
    WriteResult.success()
  }

  final private val low16Mask = 0x000000000000FFFFL
  protected def objectFile(id : Long) : File = {
    val level1 = (id >>> 48) & low16Mask
    val level2 = (id >>> 32) & low16Mask
    val level3 = (id >>> 16) & low16Mask
    val level4 = id & low16Mask
    val container = new File(dir, s"$level1/$level2/$level3")
    if (!container.isDirectory)
      if (!container.mkdirs)
        toss(s"Could not make object directory: ${container.getAbsolutePath}")

    new File(container, s"$level4.dat")
  }

  protected def readObject(id : Long) : Option[S] = {
    readObject(objectFile(id))
  }

  protected def readObject(objFile: File) : Option[S] = {
    if (!(objFile.isFile && objFile.canRead))
      None
    else {
      val fo = new FileInputStream(objFile)
      val in = new Input(fo)
      try {
        Some(KryoFormatter.kryo.readClassAndObject(in).asInstanceOf[S])
      } finally {
        in.close()
        fo.close()
      }
    }
  }

  protected def writeObject(obj: S) : WriteResult = {
    val fo = new FileOutputStream(objectFile(obj.getPrimaryId()))
    val out = new Output(fo)
    try {
      KryoFormatter.kryo.writeClassAndObject(out, obj)
      WriteResult.success()
    } catch {
      case x: Throwable ⇒
        WriteResult.failure(x)
    } finally {
      out.close()
      fo.close()
    }
  }

  protected def deleteObject(id: ID) : WriteResult = {
    try {
      val file = objectFile(id)
      if (!file.delete) {
        if (!file.exists()) {
          toss(s"File ${file.getAbsolutePath} does not exist")
        } else if (file.isDirectory) {
          toss(s"File ${file.getAbsolutePath} is a directory.")
        } else {
          toss(s"File ${file.getAbsolutePath} is locked, not writeable or missing")
        }
      }
      WriteResult.success()
    } catch {
      case x: Throwable ⇒
        WriteResult.failure(x)
    }
  }

  override def update(obj : S, upd : Modification[S]) : Future[WriteResult] = update(obj.primaryId, upd)

  override def update(id : ID, update : Modification[S]) : Future[WriteResult] = Future {
    readObject(id) match {
      case Some(s : S @unchecked) ⇒
        val newObj = update(s)
        newObj.primaryId = s.primaryId
        writeObject(newObj)
      case None ⇒
        WriteResult.error(s"Collection '$name' does not contain object with id #$id")
    }
  }

  override def insert(obj : S, update : Boolean) : Future[WriteResult] = Future.successful[WriteResult] {
    readObject(obj.primaryId) match {
      case Some(s : S @unchecked) ⇒
        if (update) {
          writeObject(obj)
        } else {
          WriteResult.error(s"Update not permitted during insert of #${obj.primaryId} in collection '$name")
        }
      case None ⇒
        ensureUniquePrimaryId(obj)
        writeObject(obj)
    }
  }

  override def fetch(id : ID) : Future[Option[S]] = Future {
    readObject(id)
  }

  override def fetchAll() : Future[Iterable[S]] = Future {
    new Iterable[S] {
      val stream = Files.walk(dir.toPath).map[S]( new java.util.function.Function[Path,S] {
        def apply(p : Path) : S = {
          val file = p.toFile
          readObject(file) match {
            case Some(obj) ⇒ obj
            case None ⇒ toss(s"Could not read object in file ${file.getAbsolutePath}")
          }
        }
      })

      def iterator: Iterator[S] = new Iterator[S] {
        val javaItr = stream.iterator()
        def hasNext: Boolean = javaItr.hasNext
        def next(): S = javaItr.next()
      }
    }
  }

  override def delete(obj : S) : Future[WriteResult] = Future {
    deleteObject(obj.getPrimaryId())
  }

  override def delete(id : ID) : Future[WriteResult] = Future {
    deleteObject(id)
  }

  override def delete(ids : Seq[ID]) : Future[WriteResult] = {
    val futures = for (id ← ids) yield {
      Future { deleteObject(id) }
    }
    WriteResult.coalesce( futures )
  }

  override def find(query : Query) : Future[Seq[S]] = Future {
    // TODO: Implement FilesCollection.find(query)
    Seq.empty[S]
  }

  override def addIndex(index : Index) : Future[WriteResult] = Future {
    // TODO: Implement FilesCollection.addIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.addIndex(field) not implemented"))
  }

  override def removeIndex(index : Index) : Future[WriteResult] = Future {
    // TODO: Implement FilesCollection.removeIndex(field) not implemented
    WriteResult.failure(new Exception("MemoryCollection.removeIndex(field) not implemented"))
  }

  override def indexOf(field : Seq[Indexable]) : Option[Index] = {
    // TODO: Implement FilesCollection.indexOf(field)
    None
  }

  override def indices : Seq[Index] = {
    // TODO: Implement FilesCollection.indices
    Seq.empty[Index]
  }

  override def updateWhere(query : Query, update : Modification[S]) : Future[Seq[WriteResult]] = Future {
    // TODO: Implement FilesCollection.updateWhere
    Seq.empty[WriteResult]
  }

}

object FilesCollection extends ScrupalComponent {

  def apply(schema: FilesSchema, collDir: File) : FilesCollection[_] = {
    require(schema.dir.isDirectory, "Schema.dir is not a directory")
    require(collDir.isDirectory, "Collections dir is not a direction")
    val infoFile = new File(collDir, FilesStorageInfo.collection_info_file_name)
    val info = if (infoFile.isFile && infoFile.canRead) {
      FilesStorageInfo.from[FilesCollectionInfo](infoFile)
    } else {
      val info = FilesCollectionInfo(collDir.getName)
      FilesStorageInfo.saveInfo(infoFile, info, overwrite=true)
      info
    }
    require(infoFile.isFile && infoFile.canRead, "Cannot read the Collection's info file")
    FilesCollection(schema, info)
  }

  def apply[S <: Storable](schema: FilesSchema, name: String) : FilesCollection[S] = {
    require(schema.dir.isDirectory)
    val collDir = new File(schema.dir, name)
    if (!(collDir.isDirectory && collDir.canRead)) {
      if (!collDir.mkdirs)
        toss(s"Could not create collection directory at ${collDir.getAbsolutePath}")
    }
    val info = FilesCollectionInfo(name)
    val infoFile = new File(collDir, FilesStorageInfo.collection_info_file_name)
    FilesStorageInfo.saveInfo(infoFile, info, overwrite=false)
    FilesCollection[S](schema, info)
  }
}