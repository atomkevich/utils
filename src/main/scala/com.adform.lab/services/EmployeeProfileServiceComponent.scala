package com.adform.lab.services

import com.adform.lab.domain.EmployeeProfile

/**
 * Created by HP on 07.02.2015.
 */

trait EmployeeProfileServiceComponent {
  def employeeProfileService: EmployeeProfileService


  trait EmployeeProfileService {
    def getEmployeeProfileByEmail(email : String): EmployeeProfile
  }
}
