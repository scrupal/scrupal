package scrupal.store.reactivemongo

/** Conversion Between JSON and BSON
  *
  * This object provides the JSONReads and JSONWrites objects needed to handle conversion between JSON format and BSON
  * format. The JSON Format objects come to us from the play-json library, with thanks!
  */
object SONConversion {

  def toBSON(json: JsValue): JsResult[BSONValue] = {
      StringFormat.pReads.
      orElse(ArrayFormat.pReads).
      orElse(ObjectIDFormat.pReads).
      orElse(DateTimeFormat.pReads).
      orElse(BinaryFormat.pReads).
      orElse(BooleanFormat.pReads).
      orElse(DoubleFormat.pReads).
      orElse(LongFormat.pReads).
      orElse(IntegerFormat.pReads).
      orElse(DBPointerFormat.pReads).
      orElse(RegexFormat.pReads).
      orElse(TimestampFormat.pReads).
      orElse(JavaScriptFormat.pReads).
      orElse(JavaScriptWSFormat.pReads).
      orElse(DocumentFormat.pReads).
      orElse(SymbolFormat.pReads).
      orElse(UndefinedFormat.pReads).
      orElse(NullFormat.pReads).
      lift(json).getOrElse(
          JsError(s"JsValue unrecognized: $json")
        )
  }

  def toJSON(bson: BSONValue): JsValue = {
    bson.code match {
      case 3 ⇒ DocumentFormat.writes(bson.asInstanceOf[BSONDocument])
      case 2 ⇒ StringFormat.writes(bson.asInstanceOf[BSONString])
      case 4 ⇒ ArrayFormat.writes(bson.asInstanceOf[BSONArray])
      case 7 ⇒ ObjectIDFormat.writes(bson.asInstanceOf[BSONObjectID])
      case 9 ⇒ DateTimeFormat.writes(bson.asInstanceOf[BSONDateTime])
      case 12 ⇒ DBPointerFormat.writes(bson.asInstanceOf[BSONDBPointer])
      case 8 ⇒ BooleanFormat.writes(bson.asInstanceOf[BSONBoolean])
      case 18 ⇒ LongFormat.writes(bson.asInstanceOf[BSONLong])
      case 16 ⇒ IntegerFormat.writes(bson.asInstanceOf[BSONInteger])
      case 1 ⇒ DoubleFormat.writes(bson.asInstanceOf[BSONDouble])
      case 5 ⇒ BinaryFormat.writes(bson.asInstanceOf[BSONBinary])
      case 11 ⇒ RegexFormat.writes(bson.asInstanceOf[BSONRegex])
      case 14 ⇒ SymbolFormat.writes(bson.asInstanceOf[BSONSymbol])
      case 6 ⇒ UndefinedFormat.writes(bson.asInstanceOf[BSONUndefined.type])
      case 10 ⇒ NullFormat.writes(bson.asInstanceOf[BSONNull.type])
      case 13 ⇒ JavaScriptFormat.writes(bson.asInstanceOf[BSONJavaScript])
      case 15 ⇒ JavaScriptWSFormat.writes(bson.asInstanceOf[BSONJavaScriptWS])
      case 17 ⇒ TimestampFormat.writes(bson.asInstanceOf[BSONTimestamp])
      case _ ⇒ throw new Exception(s"BSONValue unrecognized: $bson")
    }
  }

  /** Error Handling Utility
    * This implicit conversion just takes care of turning an exception into a JsError for us
    * @param x The exception
    * @return The corresponding JsError
    */
  implicit def jserror(x: Throwable) : JsError = JsError(x.getClass.getName + ": " + x.getMessage)

  def objhas(js: JsObject, fields: String*) = {
    js.values.size == fields.size &&
    fields.forall { f ⇒ js.value.contains(f) }
  }

  type PartialReader[T <: BSONValue] = PartialFunction[JsValue, JsResult[T]]
  type PartialWriter[T <: BSONValue] = PartialFunction[T, JsValue]

  trait BSONReader[T <:BSONValue] extends Reads[T] {
    def pReads: PartialReader[T]
    def reads(js: JsValue) = pReads.lift(js).getOrElse(JsError("JsValue unrecognized"))
  }

  trait BSONWriter[T <:BSONValue] extends Writes[T] {
    def pWrites: PartialWriter[T]
    def writes(bs: T) = pWrites(bs)
  }

  trait DoubleReader extends BSONReader[BSONDouble] {
    def pReads: PartialReader[BSONDouble] = {
      case JsNumber(f)                               ⇒ JsSuccess(BSONDouble(f.toDouble))
      case JsObject(("$double", JsNumber(v)) +: Nil) ⇒ JsSuccess(BSONDouble(v.toDouble))
    }
  }

  trait DoubleWriter extends BSONWriter[BSONDouble] {
    def pWrites: PartialWriter[BSONDouble] = {
      case double: BSONDouble ⇒ JsNumber(double.value)
    }
  }

  implicit object DoubleFormat extends Format[BSONDouble] with DoubleReader with DoubleWriter

  trait StringReader extends BSONReader[BSONString] {
    def pReads: PartialReader[BSONString] = {
      case str:JsString ⇒ JsSuccess(BSONString(str.value))
    }
  }

  trait StringWriter extends BSONWriter[BSONString] {
    def pWrites: PartialWriter[BSONString] = {
      case str: BSONString ⇒ JsString(str.value)
    }
  }

  implicit object StringFormat extends Format[BSONString] with StringReader with StringWriter

  trait DocumentReader extends BSONReader[BSONDocument] {
    def pReads: PartialReader[BSONDocument] = {
      case obj: JsObject ⇒ Try {
        val fields = obj.fields.map { pair ⇒
          val bsonValue = toBSON(pair._2) match {
            case JsSuccess(bson, _) ⇒ bson
            case JsError(err) ⇒ throw new Exception(err.toString())
          }
          pair._1 → bsonValue
        }
        BSONDocument(fields)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait DocumentWriter extends BSONWriter[BSONDocument] {
    def pWrites: PartialWriter[BSONDocument] = {
      case doc: BSONDocument ⇒
        val fields = doc.elements.map { elem ⇒ elem._1 → toJSON(elem._2) }
        JsObject(fields)
    }
  }

  implicit object DocumentFormat extends Format[BSONDocument] with DocumentReader with DocumentWriter

  trait ArrayReader extends BSONReader[BSONArray] {
    def pReads: PartialReader[BSONArray] = {
      case arr: JsArray ⇒ Try {
        val items = arr.value.map { item ⇒
          toBSON(item) match {
            case JsSuccess(bson, _) ⇒ bson
            case JsError(err)       ⇒ throw new Exception(err.toString)
          }
        }
        BSONArray(items)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait ArrayWriter extends BSONWriter[BSONArray] {
    def pWrites: PartialFunction[BSONValue, JsValue] = {
      case array: BSONArray ⇒ {
        val items = array.values.map { value ⇒ toJSON(value) }
        JsArray(items)
      }
    }
  }

  implicit object ArrayFormat extends Format[BSONArray] with ArrayReader with ArrayWriter

  trait BinaryReader extends BSONReader[BSONBinary] {
    def pReads: PartialReader[BSONBinary] = {
      case JsString(str) ⇒ Try {
        BSONBinary(Converters.str2Hex(str), Subtype.UserDefinedSubtype)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ JsError(x.getClass.getName + ": " + x.getMessage)
      }
      case JsArray(items) ⇒ Try {
        val bytes = items.map {
          case n: JsNumber if n.value <= 255 & n.value >= 0 ⇒ n.value.toByte
          case s: JsString if s.value.length == 1 ⇒ s.value.toByte
          case b: JsBoolean ⇒ if (b.value) 1.toByte else 0.toByte
          case n: JsNull.type ⇒ 0.toByte
        }
        BSONBinary(bytes.toArray, Subtype.GenericBinarySubtype)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
      case js: JsObject if js.fields.size == 2 && objhas(js, "$binary", "$type") ⇒ Try {
        val data = (js \ "$binary").as[String]
        val typ = (js \ "$type").as[String]
        val subtype = Subtype(Converters.str2Hex(typ)(0))
        BSONBinary(Converters.str2Hex(data), subtype)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait BinaryWriter extends BSONWriter[BSONBinary] {
    def pWrites: PartialWriter[BSONBinary] = {
      case binary: BSONBinary ⇒ {
        val remaining = binary.value.readable()
        Json.obj(
          "$binary" -> Converters.hex2Str(binary.value.slice(remaining).readArray(remaining)),
          "$type" -> Converters.hex2Str(Array(binary.subtype.value.toByte)))
      }
    }
  }

  implicit object BinaryFormat extends Format[BSONBinary] with BinaryReader with BinaryWriter

  trait UndefinedReader extends BSONReader[BSONUndefined.type] {
    def pReads: PartialReader[BSONUndefined.type] = {
      case _: JsUndefined ⇒ JsSuccess(BSONUndefined)
    }
  }

  trait UndefinedWriter extends BSONWriter[BSONUndefined.type] {
    def pWrites: PartialWriter[BSONUndefined.type] = {
      case BSONUndefined ⇒ JsUndefined("")
    }
  }

  implicit object UndefinedFormat extends Format[BSONUndefined.type] with UndefinedReader with UndefinedWriter

  trait ObjectIDReader extends BSONReader[BSONObjectID] {
    def pReads: PartialReader[BSONObjectID] = {
      case JsObject(("$oid", JsString(oid)) +: Nil) ⇒ JsSuccess(BSONObjectID(oid))
    }
  }

  trait ObjectIDWriter extends BSONWriter[BSONObjectID] {
    def pWrites: PartialWriter[BSONObjectID] = {
      case oid: BSONObjectID ⇒ Json.obj( "$oid" → oid.stringify )
    }
  }

  implicit object ObjectIDFormat extends Format[BSONObjectID] with ObjectIDReader with ObjectIDWriter

  trait BooleanReader extends BSONReader[BSONBoolean] {
    def pReads: PartialReader[BSONBoolean] = {
      case JsBoolean(b) ⇒ JsSuccess(BSONBoolean(b))
    }
  }

  trait BooleanWriter extends BSONWriter[BSONBoolean] {
    def pWrites: PartialWriter[BSONBoolean] = {
      case BSONBoolean(b) ⇒ JsBoolean(b)
    }
  }

  implicit object BooleanFormat extends Format[BSONBoolean] with BooleanReader with BooleanWriter

  trait DateTimeReader extends BSONReader[BSONDateTime] {
    def pReads: PartialReader[BSONDateTime] = {
      case JsObject(("$datetime", JsNumber(dt)) +: Nil) ⇒ JsSuccess(BSONDateTime(dt.toLong))
    }
  }

  trait DateTimeWriter extends BSONWriter[BSONDateTime] {
    def pWrites: PartialWriter[BSONDateTime] = {
      case dateTime: BSONDateTime ⇒ Json.obj("$datetime" -> dateTime.value)
    }
  }

  implicit object DateTimeFormat extends Format[BSONDateTime] with DateTimeReader with DateTimeWriter

  trait NullReader extends BSONReader[BSONNull.type] {
    def pReads: PartialReader[BSONNull.type] = {
      case JsNull ⇒ JsSuccess(BSONNull)
    }
  }

  trait NullWriter extends BSONWriter[BSONNull.type] {
    def pWrites: PartialWriter[BSONNull.type] = {
      case BSONNull ⇒ JsNull
    }
  }

  implicit object NullFormat extends Format[BSONNull.type] with NullReader with NullWriter

  trait RegexReader extends BSONReader[BSONRegex] {
    def pReads: PartialReader[BSONRegex] = {
      case js: JsObject if js.values.size == 2 && objhas(js, "$regex", "$flags") ⇒ Try {
        val regex = (js \ "$regex").as[String]
        val flags = (js \ "$flags").as[String]
        BSONRegex(regex, flags)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait RegexWriter extends BSONWriter[BSONRegex] {
    def pWrites : PartialWriter[BSONRegex] = {
      case regex: BSONRegex ⇒
        Json.obj("$regex" → regex.value, "$flags" → regex.flags)
    }
  }

  implicit object RegexFormat extends Format[BSONRegex] with RegexReader with RegexWriter

  trait TimestampReader extends BSONReader[BSONTimestamp] {
    def pReads: PartialReader[BSONTimestamp] = {
      case js: JsObject if js.values.size == 1 && objhas(js, "$timestamp") ⇒ Try {
        val ts = (js \ "$timestamp").as[Long]
        BSONTimestamp(ts)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait TimestampWriter extends BSONWriter[BSONTimestamp] {
    def pWrites: PartialWriter[BSONTimestamp] = {
      case ts: BSONTimestamp ⇒ Json.obj( "$timestamp" -> ts.value.toLong )
    }
  }

  implicit object TimestampFormat extends Format[BSONTimestamp] with TimestampReader with TimestampWriter

  trait DBPointerReader extends BSONReader[BSONDBPointer] {
    def pReads: PartialReader[BSONDBPointer] = {
      case js: JsObject if js.value.size == 2 && objhas(js, "$value", "$id") ⇒ Try {
        val value = (js \ "$value" ).as[String]
        val id = (js \ "$id").as[String]
        val id_bytes = Converters.str2Hex(id)
        BSONDBPointer(value, id_bytes)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait DBPointerWriter extends BSONWriter[BSONDBPointer] {
    def pWrites: PartialWriter[BSONDBPointer] = {
      case b: BSONDBPointer ⇒ Json.obj("$value" → b.value, "$id" → Converters.hex2Str(b.id))
    }
  }

  implicit object DBPointerFormat extends Format[BSONDBPointer] with DBPointerReader with DBPointerWriter

  trait JavaScriptReader extends BSONReader[BSONJavaScript] {
    def pReads: PartialReader[BSONJavaScript] = {
      case jso: JsObject if jso.values.size == 1 && objhas(jso, "$js") ⇒ Try {
        val script = (jso \ "$js").as[String]
        BSONJavaScript(script)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait JavaScriptWriter extends BSONWriter[BSONJavaScript] {
    def pWrites: PartialWriter[BSONJavaScript] = {
      case js: BSONJavaScript ⇒ Json.obj("$js" → js.value )
    }
  }

  implicit object JavaScriptFormat extends Format[BSONJavaScript] with JavaScriptReader with JavaScriptWriter

  trait JavaScriptWSReader extends BSONReader[BSONJavaScriptWS] {
    def pReads: PartialReader[BSONJavaScriptWS] = {
      case jso: JsObject if jso.values.size == 1 && objhas(jso, "$jsws") ⇒ Try {
        val script = (jso \ "$jsws").as[String]
        BSONJavaScriptWS(script)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait JavaScriptWSWriter extends BSONWriter[BSONJavaScriptWS] {
    def pWrites: PartialWriter[BSONJavaScriptWS] = {
      case js: BSONJavaScriptWS ⇒ Json.obj("$jsws" → js.value )
    }
  }

  implicit object JavaScriptWSFormat extends Format[BSONJavaScriptWS] with JavaScriptWSReader with JavaScriptWSWriter

  trait SymbolReader extends BSONReader[BSONSymbol] {
    def pReads: PartialReader[BSONSymbol] = {
      case obj: JsObject if obj.fields.size == 1 && objhas(obj, "$symbol") ⇒ Try {
        val sym = (obj \ "$symbol").as[String]
        BSONSymbol(sym)
      } match {
        case Success(x) ⇒ JsSuccess(x)
        case Failure(x) ⇒ x
      }
    }
  }

  trait SymbolWriter extends BSONWriter[BSONSymbol] {
    def pWrites: PartialWriter[BSONSymbol] = {
      case BSONSymbol(sym) ⇒ Json.obj( "$symbol" → sym)
    }
  }

  implicit object SymbolFormat extends Format[BSONSymbol] with SymbolReader with SymbolWriter

  trait IntegerReader extends BSONReader[BSONInteger] {
    def pReads: PartialReader[BSONInteger] = {
      case JsNumber(i) ⇒ JsSuccess(BSONInteger(i.toInt))
      case JsObject(("$int", JsNumber(v)) +: Nil) ⇒ JsSuccess(BSONInteger(v.toInt))
    }
  }

  trait IntegerWriter extends BSONWriter[BSONInteger] {
    def pWrites: PartialWriter[BSONInteger] = {
      case int: BSONInteger ⇒ JsObject(Seq("$int" → JsNumber(int.value)))
    }
  }

  implicit object IntegerFormat extends Format[BSONInteger] with IntegerReader with IntegerWriter

  trait LongReader extends BSONReader[BSONLong] {
    def pReads: PartialReader[BSONLong] = {
      case JsNumber(long) ⇒ JsSuccess(BSONLong(long.toLong))
      case JsObject(("$long", JsNumber(v)) +: Nil) ⇒ JsSuccess(BSONLong(v.toLong))
    }
  }

  trait LongWriter extends BSONWriter[BSONLong] {
    def pWrites: PartialWriter[BSONValue] = {
      case long: BSONLong ⇒ JsObject(Seq("$long" → JsNumber(long.value)))
    }
  }

  implicit object LongFormat extends Format[BSONLong] with LongReader with LongWriter

}
