package com.adform.lab.services.akka

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.adform.lab.ApplicationContext
import com.adform.lab.domain.{Employee, EmployeeProfile, Role}
import com.adform.lab.exceptions.FaultResult
import com.adform.lab.repositories.EmployeeRepositoryComponent
import com.adform.lab.utils.{Helper, ValidationUtils}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Await
import scala.concurrent.duration._
import scalaz._
import scalaz.syntax.applicative._
import scalaz.syntax.validation._

/**
 * Created by alina on 30.3.15.
 */
object EmployeeServiceActor {

  case class Create(email: Option[String], password: String = "test", roles: List[String] = List("Viewer"), parentId: Option[String])

  case class Search(params: Map[String, String], validation: Boolean)

  case class Update(id: String, params: Map[String, String], multi: Boolean)

  case class Get(id: String)

  case class Delete(ids: List[String])

  case class Roles(id: String, roles: List[String])

}


class EmployeeServiceActor extends Actor with ActorLogging with EmployeeRepositoryComponent with ApplicationContext {

  import EmployeeProfileServiceActor._
  import EmployeeServiceActor._
  import Helper._
  import ValidationUtils._

  implicit val profileServiceTimeout: Timeout = Timeout(50 seconds)

  implicit val timeout = Timeout

  val employeeProfileServiceActor = context.actorOf(Props[EmployeeProfileServiceActor], "employeeProfileServiceActor")


  override def receive: Receive = {

    case Search(params, true) => {
      val employees: Validation[FaultResult, List[Employee]] = getPagination(params) match {
        case Failure(err) => err.last.failure
        case Success((page, size)) =>
          employeeRepository.find(Helper.createSearchQuery(params), (page - 1) * size, page * size).success
      }

      sender ! employees
    }

    case Create(email, password, roles, parentId) => {
      sender ! (validateRole(roles, parentId) |@| emailField(email)) { (validRoles, validEmail) =>
        val savedEmployee = Employee(
          Helper.generateId,
          BCrypt.hashpw(password, BCrypt.gensalt()),
          Await.result(employeeProfileServiceActor ? Message(validEmail), 50 seconds).asInstanceOf[EmployeeProfile],
          parentId orNull,
          validRoles,
          getAncestorsById(parentId)
        )
        employeeRepository.save(savedEmployee)
        sender ! savedEmployee.successNel
      }
    }

    case Update(_, params, true) =>
      employeeRepository.multiUpdate(params)

    case Update(id, params, false) =>
      employeeRepository.get(id) match {
        case Some(employee) => {
          val updateProfile = entityToMap(employee.employeeProfile) ++ params
          employeeRepository.updateProfile(id, updateProfile)
          sender ! Left("Employee Profile was updated successfully")
        }
        case None => Right("Employee %s is not present".format(id))
      }

    case Delete(ids) => {
      employeeRepository.deleteEmployees(ids)
      employeeRepository.getByIds(ids) map (_.id) match {
        case List() => Right("employees with id in %s".format(ids.mkString("{", ",", "}" + " was deleted")))
        case notDeletesIds => Left("employees with id in %s".format(notDeletesIds.mkString("{", ",", "}" + "wasn't deleted")))
      }
    }

    case Roles(id, roles) =>
      employeeRepository.get(id) match {
        case Some(employee) => {
          validateRole(roles, Some(employee.parent)) match {
            case Success(validRoles) => {
              employeeRepository.assignRole(id, roles)
              sender ! employee.copy(roles = validRoles) successNel
            }
            case Failure(err) => sender ! err failureNel
          }
        }
        case None => "Employee %s is not present".format(id) failureNel
      }


    case Get(id) => sender ! employeeRepository.get(id)
  }

  def validateRole(roles: List[String], parentId: Option[String]): ValidationNel[FaultResult, List[Role]] = {
    employeeRepository
      .getEmployeesByRoleAndPod(roles.filter(role => "PODKeeper".equals(role) || "PODLead".equals(role)), parentId orNull) match {
      case List() => Helper.convertToRoles(roles) successNel
      case pods => FaultResult("Pod " + parentId + "already has PODKeeper or PODLead", None) failureNel
    }
  }


  def getAncestorsById(parentId: Option[String]) = parentId match {
    case Some(id) =>
      podService.getAncestorsById(id) match {
        case Some(ansList) => ansList :+ id
        case None => List()
      }
    case None => List()
  }

}