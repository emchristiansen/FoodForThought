/**
 * Copyright 2012 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package service

import scala.slick.driver.H2Driver.simple.Database
import scala.slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.driver.H2Driver.simple.Query
import scala.slick.driver.H2Driver.simple.Table
import scala.slick.driver.H2Driver.simple.booleanColumnExtensionMethods
import scala.slick.driver.H2Driver.simple.columnBaseToInsertInvoker
import scala.slick.driver.H2Driver.simple.columnExtensionMethods
import scala.slick.driver.H2Driver.simple.ddlToDDLInvoker
import scala.slick.driver.H2Driver.simple.queryToQueryInvoker
import scala.slick.driver.H2Driver.simple.valueToConstColumn
import scala.slick.jdbc.meta.MTable
import play.api.Application
import play.api.Play.current
import play.api.db.DB
import securesocial.core.AuthenticationMethod
import securesocial.core.Identity
import securesocial.core.IdentityId
import securesocial.core.PasswordInfo
import securesocial.core.UserServicePlugin
import slick.lifted.MappedTypeMapper
import java.sql.Date
import org.joda.time.DateTime
import slick.lifted.TypeMapper.DateTypeMapper
import securesocial.core.providers.Token
import scala.slick.driver.H2Driver.simple._
import play.api.Logger

//import scala.slick.driver.H2Driver
//import H2Driver.simple.Database
//import Database.{ threadLocalSession => session }
//import scala.slick.direct._
//import scala.slick.direct.AnnotationMapper._

//@table
//case class Coffee(
//  @column name: String,
//  @column(name = "PRICE") price: Double)
//
//@table
//case class Foo(
//  @column a: Int,
//  @column b: String)
//
//@table
//case class Bar(
//  @column c: Foo)

case class IdentityRecord(
  identityID_userID: String,
  identityID_providerID: String,
  firstName: String,
  lastName: String,
  fullName: String,
  email: Option[String],
  avatarUrl: Option[String],
  authMethod_method: String,
  passwordInfo_hasher: Option[String],
  passwordInfo_password: Option[String],
  passwordInfo_salt: Option[Option[String]] = None) extends Identity {
  override def identityId =
    IdentityId(identityID_userID, identityID_providerID)

  override def authMethod = AuthenticationMethod(authMethod_method)

  override def oAuth1Info = None

  override def oAuth2Info = None

  override def passwordInfo = for (
    hasher <- passwordInfo_hasher;
    password <- passwordInfo_password;
    salt <- passwordInfo_salt
  ) yield PasswordInfo(hasher, password, salt)
}

object IdentityRecords extends Table[IdentityRecord]("IdentityRecords") {
  def identityID_userID = column[String]("identityID_userID")
  def identityID_providerID = column[String]("identityID_providerID")
  def firstName = column[String]("firstName")
  def lastName = column[String]("lastName")
  def fullName = column[String]("fullName")
  def email = column[Option[String]]("email") // Option[String]
  def avatarUrl = column[Option[String]]("avatarUrl") // Option[String]
  def authMethod_method = column[String]("authMethod_method")
  def passwordInfo_hasher = column[Option[String]]("passwordInfo_hasher") // Option[String]
  def passwordInfo_password = column[Option[String]]("passwordInfo_password") // Option[String]
  def passwordInfo_salt = column[Option[Option[String]]]("passwordInfo_salt") // Option[Option[String]]

  def * =
    identityID_userID ~
      identityID_providerID ~
      firstName ~
      lastName ~
      fullName ~
      email ~
      avatarUrl ~
      authMethod_method ~
      passwordInfo_hasher ~
      passwordInfo_password ~
      passwordInfo_salt <>
      (IdentityRecord.apply _, IdentityRecord.unapply _)
}

object DateTimeMapper {

  implicit def date2dateTime = MappedTypeMapper.base[DateTime, Date](
    dateTime => new Date(dateTime.getMillis),
    date => new DateTime(date))

}

object Tokens extends Table[Token]("Tokens") {
  implicit def date2dateTime = MappedTypeMapper.base[DateTime, Date](
    dateTime => new Date(dateTime.getMillis),
    date => new DateTime(date))

  def uuid = column[String]("uuid")

  def email = column[String]("email")

  def creationTime = column[DateTime]("creationTime")

  def expirationTime = column[DateTime]("expirationTime")

  def isSignUp = column[Boolean]("isSignUp")

  def * =
    uuid ~
      email ~
      creationTime ~
      expirationTime ~
      isSignUp <> (Token.apply _, Token.unapply _)
}

/**
 * A persistent user service.
 */
class PersistentUserService(
  application: Application) extends UserServicePlugin(application) {
  def withDatabase[A](action: => A): A = Database.forDataSource(DB.getDataSource()).withSession {
    action
  }

  def ensureExistsIdentityRecords =
    withDatabase {
      if (MTable.getTables(IdentityRecords.tableName).list().isEmpty) IdentityRecords.ddl.create
    }

  def ensureExistsTokens =
    withDatabase {
      if (MTable.getTables(Tokens.tableName).list().isEmpty) Tokens.ddl.create
    }

  def find(id: IdentityId): Option[Identity] = {
    ensureExistsIdentityRecords

    if (Logger.isDebugEnabled) {
      Logger.debug("find")
      Logger.debug(id.toString)
      withDatabase {
        Query(IdentityRecords).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      val matching = Query(IdentityRecords).filter { record =>
        record.identityID_userID === id.userId && record.identityID_providerID === id.providerId
      }

      val value = matching.list.headOption
      Logger.debug(value.toString)
      value
    }
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    ensureExistsIdentityRecords

    if (Logger.isDebugEnabled) {
      Logger.debug("findByEmailAndProvider")
      Logger.debug(email)
      Logger.debug(providerId)
      withDatabase {
        Query(IdentityRecords).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      val matching = Query(IdentityRecords).filter { record =>
        record.email === email && record.identityID_providerID === providerId
      }

      val value = matching.list.headOption
      Logger.debug(value.toString)
      value
    }
  }

  def save(id: Identity): Identity = {
    ensureExistsIdentityRecords

    if (Logger.isDebugEnabled) {
      Logger.debug("save")
      Logger.debug(id.toString)
      withDatabase {
        Query(IdentityRecords).list map (s => Logger.debug(s.toString))
      }
    }

    // We first need to delete the old identity.
    withDatabase {
      Query(IdentityRecords).filter { record =>
        record.identityID_userID === id.identityId.userId &&
          record.identityID_providerID === id.identityId.providerId
      }.delete
    }

    val record = IdentityRecord(
      id.identityId.userId,
      id.identityId.providerId,
      id.firstName,
      id.lastName,
      id.fullName,
      id.email,
      id.avatarUrl,
      id.authMethod.method,
      id.passwordInfo.map(_.hasher),
      id.passwordInfo.map(_.password),
      id.passwordInfo.map(_.salt))

    withDatabase {
      IdentityRecords.insert(record)
    }

    Logger.debug(record.toString)
    record
  }

  def save(token: Token) {
    ensureExistsTokens

    if (Logger.isDebugEnabled) {
      Logger.debug("save")
      Logger.debug(token.toString)
      withDatabase {
        Query(Tokens).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      Tokens.insert(token)
    }
  }

  def findToken(uuid: String): Option[Token] = {
    ensureExistsTokens

    if (Logger.isDebugEnabled) {
      Logger.debug("findToken")
      Logger.debug(uuid)
      withDatabase {
        Query(Tokens).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      val matching = Query(Tokens).filter { token =>
        token.uuid === uuid
      }

      val value = matching.list.headOption
      Logger.debug(value.toString)
      value
    }
  }

  def deleteToken(uuid: String) {
    ensureExistsTokens

    if (Logger.isDebugEnabled) {
      Logger.debug("deleteToken")
      Logger.debug(uuid)
      withDatabase {
        Query(Tokens).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      val matching = Query(Tokens).filter { token =>
        token.uuid === uuid
      }

      matching.delete
    }
  }

  def deleteTokens() {
    ensureExistsTokens

    if (Logger.isDebugEnabled) {
      Logger.debug("deleteTokens")
      withDatabase {
        Query(Tokens).list map (s => Logger.debug(s.toString))
      }
    }

    Query(Tokens).delete
  }

  def deleteExpiredTokens() {
    ensureExistsTokens

    if (Logger.isDebugEnabled) {
      Logger.debug("deleteExpiredTokens")
      withDatabase {
        Query(Tokens).list map (s => Logger.debug(s.toString))
      }
    }

    withDatabase {
      val expiredTokens = Query(Tokens).list.filter(_.isExpired)

      for (expired <- expiredTokens) {
        deleteToken(expired.uuid)
      }
    }
  }
}
