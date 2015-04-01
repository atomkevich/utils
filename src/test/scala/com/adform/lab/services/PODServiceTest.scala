package com.adform.lab.services

import com.adform.lab.domain.{POD, PODProfile}
import com.adform.lab.repositories._
import com.adform.lab.services._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
 * Created by Alina_Tamkevich on 3/4/2015.
 */
class PODServiceTest extends FlatSpec with Matchers  with BeforeAndAfter with MockitoSugar{


  val podComponentService = new PODServiceComponentImpl with PodRepositoryComponent {
    override val podRepository = mock[PodRepository]
  }
  var adformPod: POD = _
  var epamPod: POD = _
  var facebook: POD = _
  var google: POD = _
  before {
    adformPod = POD(Some("1"), PODProfile("Adform", "Minsk", "!!!!!!"), null, null)
    epamPod = POD(Some("2"), PODProfile("Epam", "Minsk", "!!!!!!"), List("1"), "1")
    facebook = POD(Some("3"), PODProfile("Facebook", "Minsk", "!!!!!!"), List("1"), "1")
    google = POD(Some("4"), PODProfile("Google", "Minsk", "!!!!!!"), List("1", "3"), "3")
  }


  "Pod service" should "allow get pod by id" in {
    when(podComponentService.podRepository.getById("1")).thenReturn(Some(adformPod))
    podComponentService.podService.getPODById("1") match {
      case Some(actualPod) => assert(actualPod equals adformPod)
      case None => fail("Cannot get pod by id = 1")
    }
  }

  "Pod service" should "find pods by params" in {
    when(podComponentService.podRepository.find(Map("profile.location" -> "Minsk"), 0, 10)).thenReturn(List(adformPod, epamPod, facebook, google))
    when(podComponentService.podRepository.find(Map("profile.name" -> "Epam"), 0, 10)).thenReturn(List(epamPod))
    val podsByLocation = podComponentService.podService.getPODs(Map("location" -> "Minsk"))
    assert(podsByLocation.size == 4)

    val podsByName = podComponentService.podService.getPODs(Map("name" -> "Epam"))
    assert(podsByName.size == 1)
    podsByName map(pod => pod.podProfile.name == "Epam")
  }

  "Pod service" should "create new Pod by params" in {
    doNothing().when(podComponentService.podRepository).save(google)
    when(podComponentService.podRepository.getAncestorsById("3")).thenReturn(Some(List("1")))
    podComponentService.podService.createPOD(Some("3"), "Google", "Minsk", "!!!!!!") fold(pod => {
      assert(pod.podProfile.name equals google.podProfile.name)
      assert(pod.podProfile.location equals google.podProfile.location)
      assert(pod.podProfile.description equals google.podProfile.description)
      assert(pod.ancestors equals google.ancestors)
      assert(pod.parent equals google.parent)
    }, error => fail(error))
  }

  "Pod service" should "move pod to another pod" in {
    when(podComponentService.podRepository.getById("2")).thenReturn(Some(epamPod))
    doNothing().when(podComponentService.podRepository).movePOD("3", "1", List("1"))
    when(podComponentService.podRepository.getById("1")).thenReturn(Some(adformPod))
    podComponentService.podService.linkPOD("2", "3") fold(pod => {
      assert(pod.id.get equals "1")
    }, err => fail(err))
  }
}
