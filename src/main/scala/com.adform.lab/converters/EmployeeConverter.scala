package com.adform.lab.converters

import com.adform.lab.domain.{Employee, EmployeeProfile}
import com.adform.lab.utils.Helper
import com.mongodb.DBObject
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import net.liftweb.json._

import scala.util.Try

/**
 * Created by Alina_Tamkevich on 2/10/2015.
 */
object EmployeeConverter {


  def toBson(employee: Employee): DBObject = {
    MongoDBObject(
      "_id" -> employee.id,
      "password" -> employee.password,
      "parentId" -> employee.parent,
      "ancestors" -> employee.ancestors,
      "roles" -> employee.roles.map(_.name),
      "profile" -> MongoDBObject(
        "name" -> employee.employeeProfile.name,
        "email" -> employee.employeeProfile.email,
        "location" -> employee.employeeProfile.location,
        "yammerUrl" -> employee.employeeProfile.yammerUrl,
        "custom_attribute" -> employee.employeeProfile.customAttribute
      )
    )
  }

  def fromBson(document: MongoDBObject): Employee = {
    val profileDocument = document.as[MongoDBObject]("profile")
    val employeeProfile = EmployeeProfile(
      profileDocument.as[String]("name"),
      profileDocument.as[String]("email"),
      profileDocument.as[String]("location"),
      profileDocument.as[String]("yammerUrl")
    )
    Employee(
      Option(document.as[String]("_id")),
      document.as[String]("password"),
      employeeProfile,
      document.as[String]("parentId"),
      Helper.convertToRoles(Helper.fromBasicDBListToList(document.as[MongoDBList]("roles"))),
      Helper.fromBasicDBListToList(document.as[MongoDBList]("ancestors"))
    )
  }

  def toEmployeeProfile(body: String): Option[EmployeeProfile] = {
    parse(body).toOpt.map(body => EmployeeProfile(
      (body \ "name").values.asInstanceOf[String],
      (body \ "email").values.asInstanceOf[String],
      (body \ "location").values.asInstanceOf[String],
      (body \ "web_url").values.asInstanceOf[String],
      null
    )
    )
  }
}
