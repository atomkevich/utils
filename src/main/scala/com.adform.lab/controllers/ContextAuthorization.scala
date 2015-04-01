package com.adform.lab.controllers

import _root_.spray.http.HttpHeaders.Authorization
import _root_.spray.http.{BasicHttpCredentials, HttpCredentials}
import _root_.spray.routing.authentication._
import _root_.spray.routing.{AuthenticationFailedRejection, HttpService, RequestContext}
import com.adform.lab.domain.Employee
import com.adform.lab.services.EmployeeServiceComponent

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by atamkevich on 08/03/15.
 */
trait ContextAuthorization extends EmployeeServiceComponent {
  this: HttpService =>

  def doAuthenticate(token: HttpCredentials)(implicit ec: ExecutionContext): Future[Option[(Employee)]] = Future {
    token match {
      case BasicHttpCredentials(email, pass) =>
        for {
          employee <- employeeService.findEmployeeByEmail(email)
          if employee.passwordMatches(pass)
        } yield employee
      case default => None
    }
  }

  def userAuthorization(roles: String*)(implicit ec: ExecutionContext): RequestContext => Future[Authentication[Employee]] = {
    ctx: RequestContext =>
      val token = getCred(ctx)
      doAuthenticate(token.get).map {
        employee =>
          if (employee.isDefined && employee.get.hasAnyRole(roles:_*))
            Right(employee.get)
          else
            Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsRejected, Nil))
      }
  }

  def getCred(ctx: RequestContext): Option[HttpCredentials] = {
    val header = ctx.request.headers.find(_.name == "Authorization")
    header.map { case Authorization(creds) ⇒ creds }
  }
}
