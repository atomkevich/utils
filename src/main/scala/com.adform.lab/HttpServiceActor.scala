package com.adform.lab

import akka.actor.Actor
import akka.util.Timeout
import com.adform.lab.controllers.{EmployeesApi, PODApi}
import com.adform.lab.exceptions.FaultResult
import play.api.libs.json.Json
import spray.routing._
import scala.concurrent.duration._



class HttpServiceActor extends Actor with ServiceApi {
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(route)
}

 trait ServiceApi extends HttpService with ApplicationContext
        with EmployeesApi with PODApi{

  override implicit def executionContext = actorRefFactory.dispatcher
 //  override  implicit val timeout: Timeout = Timeout(500 seconds)
//
  val route =  podRoute ~ employeeRoute

}