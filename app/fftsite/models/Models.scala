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

object Models {  
  private val database = Database.forDataSource(DB.getDataSource("default"))
  
  val users =
    PersistentMap.connectElseCreate[String, SocialUser]("users", database)
  val tokens =
    PersistentMap.connectElseCreate[String, Token]("tokens", database)
}