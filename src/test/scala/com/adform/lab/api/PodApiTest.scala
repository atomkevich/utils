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
import spray.http.StatusCodes._
import java.sql.Timestamp


/**
 * Created by Alina_Tamkevich on 3/4/2015.
 */


class PodApiTest extends FlatSpec with Matchers with ScalatestRouteTest with ServiceApi {
  def actorRefFactory = system


  it should "respond on empty route" in {
    Get("/v1/pods") ~> route ~> check {
      val podResponse = response
      assert(podResponse.status.isSuccess)
    }
    Get("/v1/pods", Map("location" -> "Minsk")) ~> route ~> check {
      val podResponse = response
      assert(podResponse.status.isSuccess)
    }
    Post("/v1/pods", Map("name" -> "Google", "location" -> "Minsk", "description" -> "IT company")) ~> route ~> check {
      val podResponse = responseAs[String]
      podResponse.contains("Created")
    }

    Post("/v1/pods", Map("location" -> "Minsk")) ~> route ~> check {
      val podResponse = responseAs[String]
      podResponse.contains("Missing params")
    }
  }

 }
