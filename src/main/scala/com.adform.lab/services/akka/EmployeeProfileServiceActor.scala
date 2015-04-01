package com.adform.lab.services.akka

import akka.actor.{Actor, ActorLogging}
import com.adform.lab.converters.EmployeeConverter
import com.adform.lab.domain.EmployeeProfile
import net.liftweb.json.DefaultFormats

import scalaj.http.Http


/**
 * Created by alina on 31.3.15.
 */
object EmployeeProfileServiceActor {

  case class Message(email: String)

}

class EmployeeProfileServiceActor extends Actor with ActorLogging{
  import EmployeeProfileServiceActor._

  override def receive: Receive = {
    case Message(email) => {
      val getProfileByEmailUrl = "https://www.yammer.com/api/v1/users/by_email.json"
      val request = Http(getProfileByEmailUrl).param("email", email)
        .header("Authorization", "Bearer M58TBrGDoj2XGx51J7AQ")
        .header("Host", "www.yammer.com")
        .header("Content-Type", "application/json")


      implicit val formats = DefaultFormats
      EmployeeConverter.toEmployeeProfile(request.asString.body) match {
        case Some(employeeProfile) => sender ! employeeProfile
        case None => sender ! EmployeeProfile("anonymous", email, " - ", " - ", null)
      }
    }
  }
}
