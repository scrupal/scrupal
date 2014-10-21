package scrupal.core.api

import play.twirl.api.Html
import reactivemongo.bson.BSONDocument

/**
 * Created by reidspencer on 11/5/14.
 */
class Result[P] {

}

class BSONResult extends Result[BSONDocument]
class HTMLResult extends Result[Html]
class TextResult extends Result[String]

// class JSONResult extends Result[JsObject]
