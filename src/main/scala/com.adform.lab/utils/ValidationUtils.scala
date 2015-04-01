package com.adform.lab.utils

import com.adform.lab.domain.EmployeeFilterField
import com.adform.lab.exceptions.FaultResult

import scala.util.Try

import scalaz.ValidationNel
import scalaz.syntax.applicative._
import scalaz.syntax.validation._
/**
 * Created by alina on 27.3.15.
 */
object ValidationUtils {

  def intField(field: String): ValidationNel[FaultResult, Int] =
    Try(field.toInt) match {
      case scala.util.Success(i) => i.successNel
      case scala.util.Failure(err) =>
        FaultResult(err.getMessage, Some(err.toString)).failureNel
    }

  def emailField(field: Option[String]): ValidationNel[FaultResult, String] =
  if (field.isDefined) {
    """(\w+)@([\w\.]+)""".r.unapplySeq(field.get) match {
      case Some(email) => field.get.successNel
      case None => FaultResult("%s is not email".format(field), None).failureNel
    }
  } else FaultResult("Email cannot be empty", None) failureNel

  def notEmptyField(field: Option[String]): ValidationNel[FaultResult, String] =
      if (field.isDefined)
        field.get successNel
      else
        FaultResult("%s  cannot be empty".format(field), None) failureNel
}
