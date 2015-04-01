package com.adform.lab.services.akka

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import com.adform.lab.ApplicationContext
import com.adform.lab.domain.{POD, PODProfile}
import com.adform.lab.exceptions.FaultResult
import com.adform.lab.repositories.PodRepositoryComponent
import com.adform.lab.utils.Helper

import scalaz.syntax.validation._
import scalaz.{Failure, Success, Validation, ValidationNel}

/**
 * Created by alina on 25.3.15.
 */
object PODServiceActor {

  case class Create(parentId: Option[String], name: Option[String], location: String, description: String)

  case class Search(params: Map[String, String], validation: Boolean)

  case class Update(id: String, params: Map[String, String])

  case class Get(id: String)

  case class Delete(ids: List[String])

}

class PODServiceActor extends Actor with ActorLogging with PodRepositoryComponent with ApplicationContext {

  import Helper._
  import PODServiceActor._

  implicit val timeout = Timeout

  override def receive: Receive = {

    case Search(params, true) => {
      val pods: Validation[FaultResult, List[POD]] = getPagination(params) match {
        case Failure(err) => err.last.failure
        case Success((page, size)) => {
          podRepository.find(Helper.createSearchQuery(params), (page - 1) * size, page * size).success
        }
      }

      sender ! pods
    }

    case Search(params, false) => {
      sender ! podRepository.find(Helper.createSearchQuery(params), 0, Int.MaxValue)
    }

    case Create(parentId, name, location, description) => {
      val ancestors: List[String] = parentId match {
        case Some(id) => podRepository.getAncestorsById(id) match {
          case Some(parentAncestors) => parentAncestors :+ id
          case None => List()
        }
        case None => List()
      }
      val createdPOD: ValidationNel[FaultResult, POD] = if (name.isDefined) {
        val pod = POD(Helper.generateId, PODProfile(name.get, location, description), ancestors, parentId.orNull)
        podRepository.save(pod)
        pod.successNel
      } else FaultResult("Missing param 'name' !").failureNel

      sender ! createdPOD
    }

    case Get(id) => {
      sender ! podRepository.getById(id)
    }
    case Update(podId, params) => {
      podRepository.getById(podId) match {
        case Some(pod) => {
          val updateProfile = entityToMap(pod.podProfile) ++ params
          podRepository.updateProfile(podId, updateProfile)
          sender ! Left("POD Profile was updated successfully")
        }
        case None => {
          sender ! Right("Pod with id " + podId + " doesn't exists")
        }
      }
    }

    case Delete(ids) => {
      podRepository.deletePODs(ids)

      podRepository.findPodsByIds(ids) match {
        case List() => sender ! Left("PODs were successfully deleted")
        case pods => {
          val notDeletedIds = pods map (x => x.id) mkString("{", ",", "}")
          sender ! Right("PODs with ids in %s weren't removed".format(notDeletedIds))

        }

      }

    }
  }

}
