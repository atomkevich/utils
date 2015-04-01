package com.adform.lab.converters

import com.adform.lab.utils.Helper
import com.mongodb.{BasicDBList, BasicDBObject, DBObject}
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.adform.lab.domain.{PODProfile, POD, EmployeeProfile, Employee}

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
object PODConverter {
  def toBson(pod: POD): DBObject = {
    MongoDBObject(
      "_id" -> pod.id,
      "parentId" -> pod.parent,
      "ancestors" -> pod.ancestors,
      "profile" -> MongoDBObject(
        "name" -> pod.podProfile.name,
        "location" -> pod.podProfile.location,
        "description" -> pod.podProfile.description
      )
    )
  }

  def fromBson(document: MongoDBObject): POD = {
    val profileDocument = document.as[MongoDBObject]("profile")
    val podProfile = PODProfile(
      profileDocument.as[String]("name"),
      profileDocument.as[String]("location"),
      profileDocument.as[String]("description")
    )
    POD(
      Option(document.as[String]("_id")),
      podProfile,
      Helper.fromBasicDBListToList(document.as[MongoDBList]("ancestors")),
      document.as[String]("parentId")
    )
  }
}
