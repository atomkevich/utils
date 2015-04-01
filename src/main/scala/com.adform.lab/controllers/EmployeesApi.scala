package com.adform.lab.controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.adform.lab.domain.Employee
import com.adform.lab.exceptions.FaultResult
import com.adform.lab.services.EmployeeServiceComponent
import com.adform.lab.services.akka.EmployeeServiceActor
import play.api.libs.json._
import spray.http.MediaTypes._
import spray.http.StatusCodes.{BadRequest, Created, OK, Found}
import spray.httpx.PlayJsonSupport._
import spray.routing.HttpService

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scalaz._

trait EmployeesApi extends Authenticator {
  this: HttpService with EmployeeServiceComponent =>

  import EmployeeServiceActor._

  implicit val employeeWrites = new Writes[Employee] {

    override def writes(employee: Employee): JsValue = {
      Json.obj(
        "id" -> employee.id,
        "profile" -> Json.obj(
          "name" -> employee.employeeProfile.name,
          "email" -> employee.employeeProfile.email,
          "yammerUrl" -> employee.employeeProfile.yammerUrl,
          "location" -> employee.employeeProfile.location
        ),
        "roles" -> employee.roles.map(_.name),
        "parentId" -> employee.parent,
        "ancestors" -> employee.ancestors
      )
    }
  }

  implicit val excWrites = Json.format[FaultResult]
  implicit val createEmployeeReader = Json.format[Create]
  implicit val updateEmployeeReader = Json.format[Update]
  implicit val deleteEmployeeReader = Json.format[Delete]
  implicit val rolesEmployeeReader = Json.format[Roles]

  implicit val employeeTimeout: Timeout = Timeout(500 seconds)

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  val employeeServiceActor = actorRefFactory.actorOf(Props[EmployeeServiceActor], "employeeServiceActor")


  val employeeRoute = {
    pathPrefix("v1") {

      pathPrefix("employees") {
        get {
          authenticate(userAuthorization()) { employeeInfo =>
            pathEnd {
              parameterMap { queryParams =>
                respondWithMediaType(`application/json`) {
                  onSuccess((employeeServiceActor ? Search(queryParams, validation = true)).mapTo[Validation[FaultResult, List[Employee]]]) {
                    case Success(employees) => complete(Found, employees)
                    case Failure(errorResponse) => complete(BadRequest, errorResponse)
                  }
                }
              }
            }
          }
        } ~
          post {
            authenticate(userAuthorization("Admin", "PODLead")) { employeeInfo =>
              pathEnd {
                respondWithMediaType(`application/json`) {
                  entity(as[Create]) { create =>
                    onSuccess((employeeServiceActor ? create).mapTo[ValidationNel[FaultResult, Employee]]) {
                      case Success(employee) => complete(employee)
                      case Failure(errorResponse) => complete(errorResponse.last)
                    }
                  }
                }
              }
            }
          } ~
          put {
            authenticate(userAuthorization("Admin", "PODLead")) { employeeInfo =>
              pathEnd {
                respondWithMediaType(`application/json`) {
                  entity(as[JsObject]) { requestObj =>
                    complete {
                      val updateParams = requestObj.value.map(param => (param._1, param._2.toString)).toMap
                      employeeServiceActor ? Update("", updateParams, multi = true)
                      OK
                    }
                  }
                }
              }
            }
          } ~
          path("profile") {
            put {
              authenticate(userAuthorization("Admin", "PODLead", "User")) { employeeInfo =>
                respondWithMediaType(`application/json`) {
                  entity(as[Update]) { update =>
                    complete((employeeServiceActor ? update).mapTo[Either[String, String]])
                  }
                }
              }
            }
          } ~
          delete {
            authenticate(userAuthorization("Admin", "PODLead")) { employeeInfo =>
              respondWithMediaType(`application/json`) {
                entity(as[Delete]) { delete =>
                  complete((employeeServiceActor ? delete).mapTo[Either[String, String]])
                }
              }
            }
          }
      } ~
        pathPrefix("roles") {
          put {
            authenticate(userAuthorization("Admin", "PODLead")) { employeeInfo =>
              respondWithMediaType(`application/json`) {
                entity(as[Roles]) { roles =>
                  onSuccess((employeeServiceActor ? roles).mapTo[ValidationNel[FaultResult, Employee]]) {
                    case Success(employee) => complete(OK, employee)
                    case Failure(errorResponse) => complete(BadRequest, errorResponse.list)
                  }
                }
              }
            }
          }
        } ~
        path("employee" / Segment) { id =>
          authenticate(userAuthorization()) { employeeInfo =>
            pathEnd {
              get {
                respondWithMediaType(`application/json`) {
                  complete((employeeServiceActor ? Get(id)).mapTo[Option[Employee]])
                }
              }
            } ~
              authenticate(userAuthorization("PODLead", "Admin", "User")) { employeeInfo =>
                delete {
                  complete((employeeServiceActor ? Delete(List(id))).mapTo[Either[String, String]])
                }
              }
          }
        } ~
        path("current") {
          authenticate(userAuthorization()) { employeeInfo =>
            pathEnd {
              get {
                respondWithMediaType(`application/json`) {
                  complete(employeeInfo)
                }
              }
            }
          }
        }
    }
  }
}