package com.adform.lab


import com.adform.lab.config.MongoContext
import com.adform.lab.repositories.{EmployeeRepositoryComponentImpl, PodRepositoryComponentImpl}
import com.adform.lab.services.{EmployeeProfileServiceComponentImpl, EmployeeServiceComponentImpl, PODServiceComponentImpl}

trait ApplicationContext extends  EmployeeServiceComponentImpl
                            with EmployeeProfileServiceComponentImpl
                            with EmployeeRepositoryComponentImpl
                            with PODServiceComponentImpl
                            with PodRepositoryComponentImpl with MongoContext{
}