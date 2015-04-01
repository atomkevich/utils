package com.adform.lab.config

import com.mongodb.casbah._

/**
 * Created by Alina_Tamkevich on 2/10/2015.
 */

trait MongoContext {

  val podDB = MongoClient("localhost")("POD")

  val employeeCollection = podDB("employees")
  val podCollection = podDB("pod")
}