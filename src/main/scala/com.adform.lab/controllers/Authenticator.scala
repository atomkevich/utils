package com.adform.lab.controllers

import _root_.spray.routing.authentication.{BasicAuth, UserPass}
import _root_.spray.routing.directives.AuthMagnet
import com.adform.lab.domain.Employee
import com.adform.lab.services.EmployeeServiceComponent

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by atamkevich on 08/03/15.
 */
trait Authenticator extends EmployeeServiceComponent {
  def userAuthorization(roles: String*)(implicit ec: ExecutionContext): AuthMagnet[Employee] = {
    def validateUser(userPass: Option[UserPass]): Option[Employee] = {
      for {
        p <- userPass
        employee <- employeeService.findEmployeeByEmail(p.user)
        if employee.passwordMatches(p.pass) && employee.hasAnyRole(roles:_*)
      } yield employee
    }

    def authenticator(userPass: Option[UserPass]): Future[Option[Employee]] = Future { validateUser(userPass) }

    BasicAuth(authenticator _, realm = "Private API")
  }
}