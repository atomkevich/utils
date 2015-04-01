package com.adform.lab.api


import com.adform.lab.ServiceApi
import com.adform.lab.domain.{PODProfile, POD}
import org.scalatest.{Matchers, FlatSpec}
import org.specs2.mutable.Specification
import play.api.libs.json._
import spray.routing.HttpService
import spray.testkit.{ScalatestRouteTest, Specs2RouteTest}

import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
/**
 * Created by alina on 7.3.15.
 */
class EmployeeApiTest extends FlatSpec with Matchers with ScalatestRouteTest with ServiceApi {
  def actorRefFactory = system

  it should "respond on empty route" in {
    Get("/v1/employees") ~> route ~> check {
      val employeeResponse = response
        assert(employeeResponse.status.isSuccess)
    }
    Get("/v1/employees", Map("location" -> "Minsk")) ~> route ~> check {
      val employeeResponse = response
      assert(employeeResponse.status.isSuccess)
    }
    Post("/v1/employees", Map("email" -> "alina_tamkevich@epam.com", "location" -> "Minsk")) ~> route ~> check {
      val employeeResponse = responseAs[String]
      assert(employeeResponse.contains("alinatamkevich"))
    }
    Post("/v1/employees", Map("location" -> "Minsk")) ~> route ~> check {
      val employeeResponse = responseAs[String]
      assert(employeeResponse.contains("Missing params"))
    }
  }
}
