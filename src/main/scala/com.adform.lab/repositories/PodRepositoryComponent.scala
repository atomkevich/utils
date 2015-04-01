package com.adform.lab.repositories

import com.adform.lab.domain.{POD, Employee}

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
trait PodRepositoryComponent {
  def podRepository: PodRepository


  trait PodRepository {
    def updateProfile(podId: String, profileAttribute: Map[String, Any])
    def save(pod: POD): Unit
    def getById(id: String): Option[POD]
    def find(params: Map[String, String], skip: Int, limit: Int): List[POD]
    def getAncestorsById(id: String): Option[List[String]]
    def movePOD(id: String, parentId: String, ancestors: List[String])
    def getChildsById(id: String): List[POD]
    def deletePODs(ids: List[String])
    def findPodsByIds(ids: List[String]): List[POD]
  }
}
