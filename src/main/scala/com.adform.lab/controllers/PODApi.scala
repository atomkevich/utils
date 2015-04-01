package com.adform.lab.controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.adform.lab.domain.{POD, PODProfile}
import com.adform.lab.exceptions.FaultResult
import com.adform.lab.services.PODServiceComponent
import com.adform.lab.services.akka.PODServiceActor
import play.api.libs.json._
import spray.http.MediaTypes._
import spray.http.StatusCodes.{BadRequest, Created}
import spray.httpx.PlayJsonSupport._
import spray.routing.HttpService

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scalaz._

/**
 * Created by Alina_Tamkevich on 3/3/2015.
 */
trait PODApi extends Authenticator {
  this: HttpService with PODServiceComponent =>

  import PODServiceActor._


  implicit val podWrites = new Writes[POD] {
    override def writes(pod: POD): JsValue = {
      Json.obj(
        "id" -> pod.id,
        "profile" -> Json.obj(
          "name" -> pod.podProfile.name,
          "location" -> pod.podProfile.location,
          "description" -> pod.podProfile.description
        ),
        "parentId" -> pod.parent,
        "ancestors" -> pod.ancestors
      )
    }
  }
  implicit val podReaders = new Reads[POD] {
    override def reads(json: JsValue) = JsSuccess(new POD(
      Some((json \ "id").as[String]),
      PODProfile(
        (json \ "profile" \ "name").as[String],
        (json \ "profile" \ "location").as[String],
        (json \ "profile" \ "description").as[String]
      ),
      (json \ "ancestors").as[List[String]],
      (json \ "parentId").as[String]
    ))
  }

  implicit val errWrites = Json.format[FaultResult]
  implicit val createReads = Json.reads[Create]
  implicit val updateReads = Json.reads[Update]


  implicit val timeout: Timeout = Timeout(500 seconds)

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  val podServiceActor = actorRefFactory.actorOf(Props[PODServiceActor], "podService")

  val podRoute = pathPrefix("v1") {
    pathPrefix("pods") {
      get {
        authenticate(userAuthorization("PODLead", "Admin")) { employeeInfo =>
          pathEnd {
            parameterMap { queryParams =>
              respondWithMediaType(`application/json`) {
                onSuccess((podServiceActor ? Search(queryParams, validation = true)).mapTo[Validation[FaultResult, List[POD]]]) { res => res match {
                  case Success(pods) => complete(pods)
                  case Failure(errorResponse) => complete(errorResponse)
                }
                }
              }
            }
          }
        }
      } ~
        post {
          authenticate(userAuthorization("Admin")) { employeeInfo =>
            pathEnd {
              respondWithMediaType(`application/json`) {
                entity(as[Create]) { create =>
                  onSuccess((podServiceActor ? create).mapTo[ValidationNel[FaultResult, POD]]) {
                    case Success(pod) => complete(Created, pod)
                    case Failure(errorResponse) => complete(BadRequest, errorResponse.last)
                  }
                }
              }
            }
          }
        } ~
        path("parent" / Segment) { id =>
          authenticate(userAuthorization()) { employeeInfo =>
            get {
              respondWithMediaType(`application/json`) {
                onSuccess((podServiceActor ? Get(id)).mapTo[Option[POD]]) {
                  case Some(pod) => complete((podServiceActor ? Get(pod.parent)).mapTo[Option[POD]])
                  case None => complete(BadRequest, "POD with id " + id + " doesn't exists")
                }
              }
            }
          }
        } ~
        path("linked" / Segment) { id =>
          authenticate(userAuthorization()) { employeeInfo =>
            get {
              respondWithMediaType(`application/json`) {
                onSuccess((podServiceActor ? Get(id)).mapTo[Option[POD]]) {
                  case Some(pod) => complete((podServiceActor ? Search(Map("parentId" -> pod.parent), validation = false)).mapTo[List[POD]])
                  case None => complete(BadRequest, "POD with id " + id + " doesn't exists")
                }
              }
            }
          }

        } ~
        path("childs" / Segment) { id =>
          authenticate(userAuthorization()) { employeeInfo =>

            get {
              respondWithMediaType(`application/json`) {
                onSuccess((podServiceActor ? Get(id)).mapTo[Option[POD]]) {
                  case Some(pod) => complete((podServiceActor ? Search(Map("parentId" -> id), validation = false)).mapTo[List[POD]])
                  case None => complete(BadRequest, "POD with id " + id + " doesn't exists")
                }
              }
            }

          }
        } ~
        pathPrefix("profile") {
          put {
            authenticate(userAuthorization()) { employeeInfo =>
              respondWithMediaType(`application/json`) {
                entity(as[Update]) { update =>
                  complete((podServiceActor ? Update(update.id, update.params)).mapTo[Either[String, String]])
                }
              }
            }
          }
        }
    } ~
      path("pod" / Segment) { id =>
        pathEnd {
          get {
            authenticate(userAuthorization()) { employeeInfo =>
              respondWithMediaType(`application/json`) {
                 complete((podServiceActor ? Get(id)).mapTo[Option[POD]])
              }
            }
          } ~
            delete {
              authenticate(userAuthorization("Admin")) { employeeInfo =>
                complete((podServiceActor ? Delete(List(id))).mapTo[Either[String, String]])
              }
            }
        }
      }
  }
}