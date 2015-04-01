package com.adform.lab.domain

/**
 * Created by HP on 07.02.2015.
 */
sealed  trait Role {
  val name: String
}

case object AdminRole extends Role {
  override val name: String = "Admin"
}
case object PODLeadRole extends Role {
  override val name: String = "PODLead"
}
case object PODKeeperRole extends Role {
  override val name: String = "PODKeeper"
}
case object Viewer extends Role {
  override val name: String = "Viewer"
}

case class CustomRole(name: String) extends Role

