package com.adform.lab.services

import com.adform.lab.domain.{AdminRole, Employee, EmployeeProfile}
import com.adform.lab.repositories.EmployeeRepositoryComponent
import com.adform.lab.services.{EmployeeProfileServiceComponent, EmployeeServiceComponentImpl, PODServiceComponent}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 * Created by Alina_Tamkevich on 3/4/2015.
 */
class EmployeeServiceTest extends FlatSpec with Matchers  with BeforeAndAfter with MockitoSugar {

  val employeeComponentService = new EmployeeServiceComponentImpl with EmployeeRepositoryComponent with EmployeeProfileServiceComponent with PODServiceComponent {
    override val employeeRepository = mock[EmployeeRepository]
    override val  employeeProfileService = mock[EmployeeProfileService]
    override val podService = mock[PODService]
  }

  val testEmployee: Employee = Employee(Some("1"), BCrypt.hashpw("test", BCrypt.gensalt()), EmployeeProfile("test", "test@gmail.com", "Minsk", "yammer_url"), null, List(AdminRole), List())

  "A Employee service " should "find employees by search params" in {
    when(employeeComponentService.employeeRepository.find(Map("profile.location" -> "Minsk", "profile.name" -> "test"), 0, 10)).thenReturn(List(testEmployee))
    val employees = employeeComponentService.employeeService.getAllEmployees(Map("location" -> "Minsk", "name" -> "test"))
    assert(employees.size == 1)
    employees map(employee => {
      assert(employee equals testEmployee)
    })
  }

  "Employee service" should "create employee" in {
    doNothing().when(employeeComponentService.employeeRepository).save(testEmployee)
    when(employeeComponentService.employeeProfileService.getEmployeeProfileByEmail("test@gmail.com")).thenReturn(EmployeeProfile("test", "test@gmail.com", "Minsk", "yammer_url", null))
    val aa = employeeComponentService.employeeService.createNewEmployee("test@gmail.com", "test", List("Admin"), None) fold( employee => {
      assert(employee.employeeProfile.name equals testEmployee.employeeProfile.name)
      assert(employee.employeeProfile.location equals testEmployee.employeeProfile.location)
      assert(BCrypt.checkpw("test", employee.password))
    }, error => fail(error))
  }

  "Employee service" should "update employee profile" in {
    doNothing().when(employeeComponentService.employeeRepository).updateProfile("1", Map("profile.location" -> "Moscow", "profile.name" -> "New_Name"))
    when(employeeComponentService.employeeRepository.get("1")).thenReturn(Some(testEmployee))
    employeeComponentService.employeeService.updateProfile("1", Map("location" -> "Moscow", "name" -> "New_ Name")) fold (success => {
      assert(success contains "Success")
    }, error => fail(error))
  }
}
