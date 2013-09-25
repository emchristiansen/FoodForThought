package fftsite.models

import play.api.{ Logger, Application }

import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId

import scalatestextra._

import scala.pickling._
import scala.pickling.binary._

import scala.slick.driver.SQLiteDriver.simple._
import play.api.db.DB
import play.api.Play.current
import org.joda.time._
import scalatestextra._

case class UserInformation(studentID: Option[String], employeeID: Option[String])

case class DietaryInformation(
  restrictions: Option[String],
  preferences: Option[String],
  additionalNotes: Option[String])

object Models {
  private val database = Database.forDataSource(DB.getDataSource("default"))

  val users =
    PersistentMap.connectElseCreate[IdentityId, SocialUser]("users", database)
  val tokens =
    PersistentMap.connectElseCreate[String, Token]("tokens", database)

  val userInformation =
    PersistentMap.connectElseCreate[IdentityId, UserInformation]("userInformation", database)
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
}