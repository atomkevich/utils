package com.adform.lab.repositories

import com.adform.lab.config.MongoContext
import com.adform.lab.converters.EmployeeConverter
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.{MongoDB, MongoConnection}
import com.mongodb.casbah.commons.ValidBSONType.BasicDBObject
import com.adform.lab.domain._

import scala.collection.JavaConverters._

/**
 * Created by HP on 08.02.2015.
 */
trait EmployeeRepositoryComponentImpl extends  EmployeeRepositoryComponent{
  this: MongoContext =>

  def employeeRepository = new EmployeeRepositoryImpl

  class EmployeeRepositoryImpl extends EmployeeRepository {
    override def get(id: String): Option[Employee] = {
      employeeCollection.findOne(MongoDBObject("_id" -> id)).map(employee => EmployeeConverter.fromBson(employee))
    }

    override def delete(id: String): Unit = {
      employeeCollection.remove(MongoDBObject("_id" -> id))
    }

    override def save(employee: Employee): Unit = {
      employeeCollection.save(EmployeeConverter.toBson(employee))
    }

    override def find(search: Map[String, String], skip: Int, limit: Int): List[Employee] = {
       employeeCollection.find(search).skip(skip).limit(limit)
        .map(employee => EmployeeConverter.fromBson(employee)).toList
    }

    override def getEmployeesByRoleAndPod(role: List[String], parentId: String): List[Employee] = {
      employeeCollection.find(("role" $in role) ++ ("parentId" -> parentId))
        .map(employee => EmployeeConverter.fromBson(employee)).toList
    }

    override def assignRole(id: String, role: List[String]): Unit = {
      employeeCollection.update(MongoDBObject("_id" -> id), $set("role" -> role))
    }

    override def updateProfile(id: String, result: Map[String, Any]): Unit = {
      employeeCollection.update(MongoDBObject("_id" -> id),  $set("profile" -> result))
    }

    override def deleteEmployees(ids: List[String]): Unit = {
      employeeCollection.remove("_id" $in ids)
    }

    override def multiUpdate(updateQuery: Map[String, String]) = {
      updateQuery foreach(value => employeeCollection.update(MongoDBObject.empty, $set(value), multi = true))

    }

    override def getByEmail(email: String): Option[Employee] = {
      employeeCollection.findOne(MongoDBObject("profile.email" -> email.toLowerCase))
        .map(employee => EmployeeConverter.fromBson(employee))
    }

    override def getByIds(ids: List[String]): List[Employee] = {
      employeeCollection.find("_id" $in ids)
        .map(employee => EmployeeConverter.fromBson(employee)).toList
    }
  }
}
