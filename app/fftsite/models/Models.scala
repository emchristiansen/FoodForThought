package fftsite.models

import play.api.{ Logger, Application }

import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId

import scalatestextra._

import scala.pickling._
import scala.pickling.binary._

import scala.slick.session.Database
import play.api.db.DB
import play.api.Play.current
import org.joda.time._
import scalatestextra._
import st.sparse.persistentmap._
import st.sparse.persistentmap.CustomPicklers._

case class YearAndQuarter(
  year: Int,
  quarter: Int) {
  assert(quarter >= 0)
  assert(quarter < 4)

  def prettyString = year + " " + (quarter match {
    case 0 => "winter"
    case 1 => "spring"
    case 2 => "summer"
    case 3 => "fall"
  })
  
  def toSortable = year * 10 + quarter
}

case class EmploymentStatus(status: String) {
  assert(status == "Student" || status == "Employee" || status == "Neither")
}

//sealed trait EmploymentStatus { def prettyString: String }
//object Student extends EmploymentStatus {
//  override def prettyString = "Student"
//}
//object Employee extends EmploymentStatus {
//  override def prettyString = "Employee"
//}
//object Neither extends EmploymentStatus {
//  override def prettyString = "Neither"
//}

case class UserInformation(
  studentID: Option[String],
  employeeID: Option[String])

case class EmploymentHistory(history: Map[YearAndQuarter, EmploymentStatus])

case class DietaryInformation(
  restrictions: Option[String],
  preferences: Option[String],
  additionalNotes: Option[String])

case class ReimbursementPart(
  date: LocalDate,
  expenseType: String,
  amount: BigDecimal,
  notes: String)

case class ReimbursementRequest(
  uuid: Long,
  reimbursementPart: ReimbursementPart,
  receiptPhotoName: String)

object Models {
  private val database = Database.forDataSource(DB.getDataSource("default"))

  val users =
    PersistentMap.connectElseCreate[IdentityId, SocialUser]("users", database)
  val tokens =
    PersistentMap.connectElseCreate[String, Token]("tokens", database)

  val userInformation =
    PersistentMap.connectElseCreate[IdentityId, UserInformation]("userInformation", database)
  val employmentHistory =
    PersistentMap.connectElseCreate[IdentityId, EmploymentHistory]("employmentHistory", database)
  val dietaryInformation =
    PersistentMap.connectElseCreate[IdentityId, DietaryInformation]("dietaryInformation", database)

  val freshFoodSignUp =
    PersistentMap.connectElseCreate[LocalDate, IdentityId]("freshFoodSignUp", database)
  //  val stockedFoodSignUp =
  //    PersistentMap.connectElseCreate[LocalDate, IdentityId]("stockedFoodSignUp", database)
  val cleaningSignUp =
    PersistentMap.connectElseCreate[LocalDate, IdentityId]("cleaningSignUp", database)

  val mealsSignUp =
    PersistentMap.connectElseCreate[LocalDate, Map[IdentityId, Int]]("mealsSignUp", database)

  val reimbursementRequests =
    PersistentMap.connectElseCreate[IdentityId, Set[ReimbursementRequest]]("reimbursementRequests", database)
}

trait PicklerImplicits {

}