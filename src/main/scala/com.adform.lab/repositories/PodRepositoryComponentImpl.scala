package com.adform.lab.repositories

import com.adform.lab.config.MongoContext
import com.adform.lab.converters.PODConverter
import com.adform.lab.utils.Helper
import com.mongodb.casbah.Imports._
import com.mongodb.{BasicDBList}
import com.adform.lab.domain.POD

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
trait PodRepositoryComponentImpl extends PodRepositoryComponent{
  this: MongoContext =>

    def podRepository = new PodRepositoryImpl

      class PodRepositoryImpl extends  PodRepository {
        override def getAncestorsById(id: String): Option[List[String]] = {
           podCollection.findOne(MongoDBObject("_id" -> id))
             .map(pod => pod.get("ancestors").asInstanceOf[BasicDBList]).map(ancestors =>  Helper.fromBasicDBListToList(ancestors))
        }

        override def save(pod: POD): Unit = {
          podCollection.save(PODConverter.toBson(pod))
        }

        override def getById(id: String): Option[POD] = {
           podCollection.findOne(MongoDBObject("_id" -> id)).map(item => PODConverter.fromBson(item))
        }

        override def find(params: Map[String, String], skip: Int, limit: Int): List[POD] = {
           podCollection.find(params).skip(skip).limit(limit).map(pod => PODConverter.fromBson(pod)).toList
        }

        override def updateProfile(podId: String, profileAttribute: Map[String, Any]): Unit = {
          podCollection.update(MongoDBObject("_id"-> podId), $set("profile" -> profileAttribute))
        }

        override def movePOD(id: String, parentId: String, ancestors: List[String]): Unit = {
          podCollection.update(MongoDBObject("_id" -> id), $set("parentId" -> parentId, "ancestors" -> ancestors))
          podCollection.update(MongoDBObject("parentId" -> id), $set("ancestors" -> (ancestors :+ id)))
        }

        override def getChildsById(id: String): List[POD] = {
          podCollection.find(MongoDBObject("parentId" -> id)).map(pod => PODConverter.fromBson(pod)).toList
        }

        override def deletePODs(ids: List[String]): Unit = {
          podCollection.remove("_id" $in ids)
          podCollection.remove("ancestors" $in ids)
        }

        override def findPodsByIds(ids: List[String]): List[POD] = {
          podCollection.find("_id" $in ids).map(pod => PODConverter.fromBson(pod)).toList
        }
      }
}
