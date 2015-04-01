package com.adform.lab.domain

/**
 * Created by Alina_Tamkevich on 2/9/2015.
 */
case class EmployeeProfile(name: String,
                           email: String,
                           location: String,
                           yammerUrl: String,
                           customAttribute: Map[String,String] = Map.empty[String,String]) {

  def addProfileAttribute(key: String, value: String): EmployeeProfile =
    copy(customAttribute = this.customAttribute ++ Map(key -> value))

}
