package com.adform.lab.services

import com.adform.lab.domain.Employee

/**
 * Created by HP on 08.02.2015.
 */
trait EmployeeServiceComponent {
  def employeeService: EmployeeService


  trait EmployeeService {
    def multiUpdate(update: (String, String))
    def createNewEmployee(email : String, password: String, role: List[String], parentId: Option[String]): Either[Employee, String]
    def getAllEmployees(params: Map[String, String]): List[Employee]
    def getEmployeeById(id: String): Option[Employee]
    def assignRoles(id: String, roles: List[String]): Either[Unit, String]
    def updateProfile(id: String, fields: Map[String, String]): Either[String, String]
    def deleteEmployees(ids: List[String])
    def findEmployeeByEmail(email: String): Option[Employee]

  }
}
