package com.adform.lab.domain

import org.mindrot.jbcrypt.BCrypt

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
case class Employee(id: Option[String],
                    password: String,
                    employeeProfile: EmployeeProfile,
                    parent: String,
                    roles: List[Role],
                    ancestors: List[String]) {
  def passwordMatches(password: String) = BCrypt.checkpw(password, this.password)


  def hasAnyRole(roles: String*) = if (roles.isEmpty)
    true
  else this.roles.map(_.name).intersect(roles).nonEmpty
}