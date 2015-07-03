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

package scrupal.core.providers

import play.api.routing.sird._

import scrupal.api._

import scala.concurrent.{ExecutionContext, Future}

/** A Provider Of Entity API Documentation
  * This provider can generate the documentation for any entities that are reachable in the current context.
  * Each of the standard HTTP Method/Path pairs that entities respond to are provided in the routing for this
  * provider and will respond with HTML documentation for the kind of entity requested.
  */
case class APIDoc() extends { val id : Symbol = 'apidoc } with SingularProvider {
  def singularRoutes : ReactionRoutes = {
    case GET(p"/$kind/GET/${long(num)}/$facet/$facet_id$rest*") ⇒
      GetNumDoc(kind, num, facet, facet_id, rest)
    case GET(p"/$kind/GET/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      GetNameDoc(kind, name, facet, facet_id, rest)
    case GET(p"/$kind/HEAD/${long(num)}/$facet/$facet_id$rest*") ⇒
      FacetInfoNumDoc(kind, num, facet, facet_id, rest)
    case GET(p"/$kind/HEAD/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      FacetInfoNameDoc(kind, name, facet, facet_id, rest)
    case GET(p"/$kind/OPTIONS/${long(num)}/$facet$rest*") ⇒
      FindNumDoc(kind, num, facet, rest)
    case GET(p"/$kind/OPTIONS/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      FindNameDoc(kind, name, facet, rest)
    case GET(p"/$kind/POST/${long(num)}/$facet$rest*") ⇒
      AddNumDoc(kind, num, facet, rest)
    case GET(p"/$kind/POST/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet$rest*") ⇒
      AddNameDoc(kind, name, facet, rest)
    case GET(p"/$kind/PUT/${long(num)}/$facet/$facet_id$rest*") ⇒
      SetNumDoc(kind, num, facet, facet_id, rest)
    case GET(p"/$kind/PUT/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      SetNameDoc(kind, name, facet, facet_id, rest)
    case GET(p"/$kind/DELETE/${long(num)}/$facet/$facet_id$rest*") ⇒
      RemoveNumDoc(kind, num, facet, facet_id, rest)
    case GET(p"/$kind/DELETE/$name<[A-Za-z][-_.~a-zA-Z0-9]*>/$facet/$facet_id$rest*") ⇒
      RemoveNameDoc(kind, name, facet, facet_id, rest)

    case GET(p"/$kind/GET/${long(num)}$rest*") ⇒
      RetrieveNumDoc(kind, num, rest)
    case GET(p"/$kind/GET/$name<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      RetrieveNameDoc(kind, name, rest)
    case GET(p"/$kind/HEAD/${long(num)}$rest*") ⇒
      InfoNumDoc(kind, num, rest)
    case GET(p"/$kind/HEAD/$name<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      InfoNameDoc(kind, name, rest)
    case GET(p"/$kind/OPTIONS$rest*") ⇒
      QueryDoc(kind, rest)
    case GET(p"/$kind/POST$rest*") ⇒
      CreateDoc(kind, rest)
    case GET(p"/$kind/PUT/${long(num)}$rest*") ⇒
      UpdateNumDoc(kind, num, rest)
    case GET(p"/$kind/PUT/$name<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      UpdateNameDoc(kind, name, rest)
    case GET(p"/$kind/DELETE/${long(num)}$rest*") ⇒
      DeleteNumDoc(kind, num, rest)
    case GET(p"/$kind/DELETE/$name<[A-Za-z][-_.~a-zA-Z0-9]*>$rest*") ⇒
      DeleteNameDoc(kind, name, rest)
    case GET(p"/$kind") ⇒
      EntityIntroduction(kind)
    case GET(p"") ⇒
      ApiDocIntroduction()
  }

  // TODO: Implement the APIDoc Reactors to provide documentation for Entities

  case class RetrieveNumDoc(kind: String, num: Long, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, retrieval of instance $num with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class RetrieveNameDoc(kind: String, nam: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, retrieval of instance $nam with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class InfoNumDoc(kind: String, num: Long, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, retrieval of info for instance $num with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class InfoNameDoc(kind: String, nam: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, retrieval of info for instance $nam with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class QueryDoc(kind: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, query with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class CreateDoc(kind: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, create with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class UpdateNumDoc(kind: String, num: Long, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, update $num with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class UpdateNameDoc(kind: String, nam: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, update $nam with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class DeleteNumDoc(kind: String, num: Long, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, delete $num with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class DeleteNameDoc(kind: String, nam: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, delete $nam with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class EntityIntroduction(kind: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, introduction"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class ApiDocIntroduction() extends Reactor {
    def description: String = "Entity API Introduction"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class GetNumDoc(kind : String, num: Long, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, get $num/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class GetNameDoc(kind : String, nam: String, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, get $nam/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class FacetInfoNumDoc(kind : String, num: Long, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, facet Info for $num/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class FacetInfoNameDoc(kind : String, nam: String, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, facet Info for $nam/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }

  case class FindNumDoc(kind : String, num: Long, facet: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, find for $num/$facet with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class FindNameDoc(kind : String, nam: String, facet: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, find for $nam/$facet with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class AddNumDoc(kind : String, num: Long, facet: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, add for $num/$facet with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class AddNameDoc(kind : String, nam: String, facet: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, find for $nam/$facet with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class SetNumDoc(kind : String, num: Long, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, set for $num/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class SetNameDoc(kind : String, nam: String, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, set for $nam/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class RemoveNumDoc(kind : String, num: Long, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, remove for $num/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec: ExecutionContext ⇒ Future {NoopResponse} }
    }
  }
  case class RemoveNameDoc(kind : String, nam: String, facet: String, facet_id: String, rest: String) extends Reactor {
    def description: String = s"Documentation for Entity type $kind, remove for $nam/$facet/$facet_id with $rest"
    def apply(stimulus: Stimulus) : Future[Response] = {
      stimulus.context.withExecutionContext { implicit ec : ExecutionContext ⇒
        Future {
          NoopResponse
        }
      }
    }
  }


  /*
    val apidocs = CoreFeatures.RESTAPIDocumentation

    /** Provide an introduction to the API */
    def introduction() = UserAction { implicit context: AnyUserContext ⇒
      WithFeature(apidocs) {
        Ok(html.api.introduction(modules, types))
      }
    }

    def fetchAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒
      WithFeature(apidocs) {
        val (singular, description, module) = kind.toLowerCase() match {
          case "sites" ⇒    ("site", "A site that Scrupal is configured to serve", "Core")
          case "modules" ⇒  ("module", "A Scrupal Plug-in that extends it's functionality", "Core")
          case "entities" ⇒ ("entity", "The kinds of entities that Scrupal is configured to serve", "Core")
          case "types" ⇒    ("type", "The data types that Scrupal is configured to serve", "Core" )
          case "instances" ⇒ ("instance", "The entity instances Scrupl is configured to serve", "Core")
          case _ ⇒          (kind, "No further description available.", "Unknown")
        }

        Ok(html.api.fetchAll(singular, Pluralizer.pluralize(singular), description, module))
      }
    }

    def fetch(kind: String, id: String) = UserAction.async { implicit context: AnyUserContext ⇒
      AsyncWithFeature(apidocs) {
        kind.toLowerCase match {
          case "type" ⇒  Future {
            Type(Symbol(id)) match {
              case t: Some[Type] ⇒ Ok(html.api.fetchType(t.get))
              case _ ⇒ NotFound("Type " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "module" ⇒ Future {
            Module(Symbol(id)) match {
              case m: Some[Module] ⇒ Ok(html.api.fetchModule(m.get))
              case _ ⇒ NotFound("Module " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "site" ⇒ Future {
            Site(Symbol(id)) match {
              case s: Some[Site] ⇒ Ok(html.api.fetchSite(s.get))
              case _ ⇒ NotFound("Site " + id, Seq("You mis-typed '" + id + "'?"))
            }
          }
          case "entity" ⇒ Future {
            Entity(Symbol(id)) match {
              case e: Some[Entity] ⇒ Ok(html.api.fetchEntity(e.get))
              case _ ⇒ NotFound("Entity " + id, Seq())
            }
          }
          case _ ⇒ Entity(Symbol(kind)) match {
            case e: Some[Entity] ⇒ {
              context.schema.instances.fetch(Symbol(id)) map { os: Option[Instance]⇒
                os match {
                  case Some(instance) ⇒ Ok(html.api.fetchInstance(Symbol(id), instance))
                  case None ⇒ NotFound("the " + kind + " " + id, Seq("the " + kind + " was not found"))
                }
              }
            }
            case _ ⇒ Future { NotFound("fetch of " + kind + " " + id, Seq("You mis-typed '" + kind + "'?") ) }
          }
        }
      }
    }

    def createAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " not finished."))
    }}

    def create(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Creation of " + kind + " " + id + " not finished."))
    }}

    def deleteAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of all " + kind + " not finished."))
    }}

    def delete(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Deletion of " + kind + " " + id + " not finished."))
    }}

    def updateAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " not finished."))
    }}

    def update(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Update of " + kind + " " + id + " not finished."))
    }}

    def summarizeAll(kind: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + kind + " not finished."))
    }}

    def summarize(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Info for " + id + " of kind " + kind + " not finished."))
    }}

    def optionsOfAll(kind : String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " not finished."))
    }}

    def optionsOf(kind: String, id: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Options of " + kind + " for " + id + " not finished."))
    }}

    def doTo(kind: String, id: String, action: String) = UserAction { implicit context: AnyUserContext ⇒ WithFeature(apidocs) {
      NotImplemented(apidocs, Some("Doing " + action + "to " + id + " of kind " + kind + " not finished."))
    }}
    */
}
