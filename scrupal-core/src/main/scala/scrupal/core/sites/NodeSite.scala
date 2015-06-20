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

package scrupal.core.sites

import org.joda.time.DateTime
import reactivemongo.bson.{ BSONDocument, Macros }
import scrupal.core.actions.NodeReaction
import scrupal.core.api._
import scrupal.db.VariantReaderWriter

case class NodeSite(
  override val id : Identifier,
  name : String,
  description : String,
  host : String,
  siteRoot : Node = Node.Empty,
  override val requireHttps : Boolean = false,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None) extends Site(id) {
  final override val kind = NodeSite.kind

  override def extractAction(context : Context) : Option[Action] = {
    super.extractAction(context) match {
      case Some(action) ⇒ Some(action)
      case None ⇒ Some(NodeReaction(context, siteRoot))
    }
  }
}

object NodeSite {
  import BSONHandlers._

  implicit val nodeReader = Node.NodeReader
  implicit val nodeWriter = Node.NodeWriter

  object NodeSiteBRW extends VariantReaderWriter[Site, NodeSite] {
    implicit val NodeSiteHandler = Macros.handler[NodeSite]
    override def fromDoc(doc : BSONDocument) : NodeSite = NodeSiteHandler.read(doc)
    override def toDoc(obj : Site) : BSONDocument = NodeSiteHandler.write(obj.asInstanceOf[NodeSite])
  }
  val kind = 'NodeSite
  Site.variants.register(kind, NodeSiteBRW)
}

