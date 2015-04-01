package com.adform.lab.services

import _root_.akka.actor.Actor
import _root_.akka.actor.Actor.Receive
import com.adform.lab.domain.{POD, PODProfile}
import com.adform.lab.repositories.PodRepositoryComponent
import com.adform.lab.utils.Helper

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
trait PODServiceComponentImpl extends PODServiceComponent{
  this: PodRepositoryComponent =>
  override def podService: PODService = new PODServiceImpl

  class PODServiceImpl extends PODService {
    override def getAncestorsById(id: String): Option[List[String]] = podRepository.getAncestorsById(id)

    override def createPOD(parentId: Option[String], name: String, location: String, description: String): Either[POD, String] = {

      val ancestors: List[String] = parentId match {
        case Some(id) => podRepository.getAncestorsById(id) match {
          case Some(ancestors) => ancestors :+ id
          case None => List()
        }
        case None => List()
      }

      val createdPOD = POD(Helper.generateId, PODProfile(name, location, description), ancestors, parentId.getOrElse(null))
      podRepository.save(createdPOD)
      Left(createdPOD)
    }

    override def getPODById(id: String): Option[POD] = podRepository.getById(id)

    override def getPODs(params: Map[String, String]): List[POD] = {
      val page = 1
      val size = 10
      podRepository.find(Helper.createSearchQuery(params), (page-1)*size, page*size)
    };

    override def updateProfile(podId: String, params: Map[String, String]): Either[String, String] = {
      podRepository.getById(podId) match {
        case Some(pod) => {
          val updateProfile = Map("name" -> pod.podProfile.name, "location" -> pod.podProfile.location, "description" -> pod.podProfile.description) ++ params
          podRepository.updateProfile(podId, updateProfile)
          Left("POD Profile was updated successfully")
        }
        case None => Right("Pod with id" + podId + "doesn't exists")
      }
    }

    override def linkPOD(firstPodId: String, secondPodId: String): Either[POD, String] = {
     podRepository.getById(firstPodId) match {
       case Some(firstPOD) => {
         podRepository.movePOD(secondPodId, firstPOD.parent, firstPOD.ancestors)
         podRepository.getById(firstPOD.parent) match {
           case Some(pod) => Left(pod)
           case None => Right("Cannot link pods { %s, %s}".format(firstPodId, secondPodId))
         }
       }
       case None => Right("Cannot link pods because pod " + firstPodId + " doesn't exists")
     }


    }

    override def getPODChildsById(id: String): List[POD] = {
      podRepository.getChildsById(id)
    }

    override def getPODLinksById(id: String): List[POD] = {
      podRepository.getById(id).map(x => podRepository.getChildsById(x.parent)).getOrElse(List())
    }

    override def getParentPOD(id: String): Option[POD] = {
      val pod = podRepository.getById(id)
      podRepository.getById(pod.get.parent)
    }

    override def deletePODs(ids: List[String]): Unit = {
      podRepository.deletePODs(ids)
    }


  }
}
