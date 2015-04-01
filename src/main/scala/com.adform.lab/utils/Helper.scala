package com.adform.lab.utils

import com.adform.lab.domain._
import com.adform.lab.exceptions.FaultResult
import com.mongodb.casbah.commons.MongoDBList
import org.bson.types.ObjectId
import scalaz.Scalaz._
import scalaz.ValidationNel

/**
 * Created by Alina_Tamkevich on 2/11/2015.
 */
object Helper {
  val convertParam: Map[String, String] =
    Map(
    "name" -> "profile.name",
    "email" -> "profile.email",
    "location" -> "profile.location",
    "description" -> "profile.description"
  )


  def convertToRoles(roles: List[String]): List[Role] = {
    roles match  {
      case List() => List(Viewer)
      case rolesList =>  rolesList map {
        case "Admin" => AdminRole
        case "PODLead" => PODLeadRole
        case "PODKeeper" => PODKeeperRole
        case roleName: String => CustomRole(roleName)
        case _ => Viewer
      }
    }
  }


  def generateId = Option(ObjectId.get().toString)

  def fromBasicDBListToList(dbList: MongoDBList):List[String] = {
    if (Option(dbList).isDefined)
       dbList.map(_.toString).toList else null
  }

  def createSearchQuery(params: Map[String, String]): Map[String, String] = {
    val filteredSearchParam = defaultEmployeeProfileAttrubute(params)
    filteredSearchParam.map({case (k, v) => (convertParam.getOrElse(k, k), v)})
  }

  def defaultEmployeeProfileAttrubute(params: Map[String, String]): Map[String, String] = {
    params.filter({case (x, y) => EmployeeFilterField.containsKey(x) })
  }
  def getPagination(params:Map[String, String]): ValidationNel[FaultResult, (Int, Int)] = {
    val pageNumber = ValidationUtils.intField(params.getOrElse("page", "1"))
    val pageSize = ValidationUtils.intField(params.getOrElse("size", "10"))
    (pageNumber |@| pageSize) {(_, _)}
  }

  def getCustomAttribute(params: Map[String, String]) ={
    params.filter({case(x, y) => !EmployeeFilterField.containsKey(x)})
  }

  def entityToMap(entity: Product) = {
    entity.getClass.getDeclaredFields.map( _.getName ) // all field names
      .zip(entity.productIterator.to ).toMap
  }

}
