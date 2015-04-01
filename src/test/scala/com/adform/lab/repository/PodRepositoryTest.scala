package com.adform.lab.repository


import com.adform.lab.config.MongoContext
import com.adform.lab.domain.{POD, PODProfile}
import com.adform.lab.repositories.{PodRepositoryComponent, PodRepositoryComponentImpl}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 * Created by Alina_Tamkevich on 3/4/2015.
 */
class PodRepositoryTest extends FlatSpec with Matchers  with BeforeAndAfter with MockitoSugar {
  var podRepositoryComponent:PodRepositoryComponent =_
  var adformPod: POD = _
  var epamPod: POD = _
  var facebook: POD = _
  var google: POD = _


  before {
    adformPod = POD(Some("1"), PODProfile("Adform", "Minsk", "!!!!!!"), List(), "")
    epamPod = POD(Some("2"), PODProfile("Epam", "Minsk", "!!!!!!"), List("1"), "1")
    facebook = POD(Some("3"), PODProfile("Facebook", "Minsk", "!!!!!!"), List("1"), "1")
    google = POD(Some("4"), PODProfile("Google", "Minsk", "!!!!!!"), List("1", "3"), "3")
    podRepositoryComponent = new PodRepositoryComponentImpl with MongoContext

  }
  "A pod repository" should "save entities" in {
    podRepositoryComponent.podRepository.save(adformPod)
    podRepositoryComponent.podRepository.save(epamPod)
    podRepositoryComponent.podRepository.save(facebook)
    podRepositoryComponent.podRepository.save(google)
    val pods = podRepositoryComponent.podRepository.find(Map("_id" -> google.id.get), 0, 10)
    assert(pods.size == 1)
  }

  "A pod repository" should "find entities" in {
    val pods = podRepositoryComponent.podRepository.find(Map("profile.location" -> "Minsk", "profile.name" -> "Google"), 0, 10)
    assert(!pods.isEmpty)
    pods.map(pod => {
      assert(pod.podProfile.name equals google.podProfile.name)
      assert(pod.podProfile.location equals google.podProfile.location)
    })
  }

 "A pod repository" should "get childs by id" in {
   val childs = podRepositoryComponent.podRepository.getChildsById("1")
   assert(childs.size == 2)
   childs.map {
     child => assert(child.parent equals "1")
   }
 }

  "A pod repository" should "move pod to another super pod" in {
    podRepositoryComponent.podRepository.movePOD("4", "2", List("1","2"))
    podRepositoryComponent.podRepository.getById("4") map(pod => assert(pod.parent equals "2"))
  }

}
