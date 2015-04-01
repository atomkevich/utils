package com.adform.lab.repositories

import com.adform.lab.domain.Employee

/**
 * Created by HP on 08.02.2015.
 */
trait EmployeeRepositoryComponent {

  def employeeRepository: EmployeeRepository


  trait EmployeeRepository {
    def getByEmail(email: String): Option[Employee]
    def multiUpdate(updateQuery: Map[String, String])
    def deleteEmployees(strings: List[String])
    def get(id: String): Option[Employee]
    def getByIds(ids: List[String]): List[Employee]
    def save(employee: Employee)
    def delete(id : String)
    def assignRole(id: String, role: List[String])
    def find(search: Map[String, String], skip: Int, limit : Int): List[Employee]
    def getEmployeesByRoleAndPod(role: List[String], parentId: String): List[Employee]
    def updateProfile(id: String, result: Map[String, Any])
  }
}
