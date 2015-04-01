package com.adform.lab.domain

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
case class POD(id: Option[String],
               var podProfile : PODProfile,
               var ancestors : List[String],
               var parent: String)
