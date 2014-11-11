package scrupal.utils

trait PathWalker[D,A,V] extends ScrupalComponent {
  protected def isDocument(v: V) : Boolean
  protected def isArray(v: V) : Boolean
  protected def asDocument(v: V) : D
  protected def asArray(v: V) : A
  protected def indexDoc(key: String, d: D) : Option[V]
  protected def indexArray(index: Int, a: A) : Option[V]
  protected def arrayLength(a: A) : Int

  def lookup(path: String, document: D) : Option[V] = {

    def error(msg: String, head: Seq[String], paths: Seq[String]) = {
      toss(s"${msg} at path '${head.mkString(".")}.${
        paths.head
      }' with remaining path elements: ${
        paths.tail.mkString(".")
      }")
    }

    def getAsDocument(head: Seq[String], paths: Seq[String], value: V) : D = {
      if (isDocument(value)) {
        asDocument(value)
      } else {
        error("Selected value was not a document so it could not be indexed", head, paths)
      }
    }

    def resolve(head: Seq[String], paths: Seq[String], doc: D) : Option[V] = {
      if (paths.isEmpty)
        return None
      val name = paths.head
      require(paths.length > 0)
      val deref : Option[V] = if (name.endsWith("]")) {
        val beginIndex = name.lastIndexOf("[") + 1
        if (beginIndex <= 0)
          error("Malformed array subscripts", head, paths)
        else {
          val endIndex = name.length - 1
          val index = name.substring(beginIndex, endIndex).toInt
          val rootName = name.substring(0, beginIndex-1)
          indexDoc(rootName, doc) match {
            case Some(value) ⇒
              if (isArray(value)) {
                val anArray = asArray(value)
                if (index < 0 || index > arrayLength(anArray)-1)
                  error(s"Array subscript out of bounds ", head, paths)
                else {
                  indexArray(index, anArray)
                }
              } else
                error("Selected value was not an array so it could not be indexed", head, paths)
            case None ⇒ None
          }
        }
      } else {
        indexDoc(name, doc) match {
          case Some(value) ⇒ Some(value)
          case None ⇒ None
        }
      }
      if (paths.length == 1) {
        deref
      } else if (deref.isEmpty) {
        None
      } else {
        val nextDoc = getAsDocument(head, paths, deref.get)
        resolve(head :+ name, paths.tail, nextDoc)
      }
    }
    val parts = path.split("\\.").toSeq
    resolve(Seq.empty[String], parts, document)
  }

}

object MapSeqPathWalker extends PathWalker[Map[String,Any],Seq[Any],Any] {
  protected def isDocument(v: Any): Boolean = v.isInstanceOf[Map[String,Any]]
  protected def asDocument(v: Any): Map[String,Any] = v.asInstanceOf[Map[String,Any]]
  protected def indexDoc(key: String, d: Map[String,Any]): Option[Any] = d.get(key)
  protected def isArray(v: Any): Boolean = v.isInstanceOf[Seq[Any]]
  protected def asArray(v: Any): Seq[Any] = v.asInstanceOf[Seq[Any]]
  protected def indexArray(index: Int, a: Seq[Any]): Option[Any] = Some(a(index))
  protected def arrayLength(a: Seq[Any]): Int = a.size
  def apply(path: String, doc: Map[String,Any]) : Option[Any] = lookup(path, doc)

}
