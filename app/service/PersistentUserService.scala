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

import play.api.Application
import play.api.Logger
import securesocial.core._
import securesocial.core.IdentityId
import securesocial.core.UserServicePlugin
import securesocial.core.providers.Token

import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.jdbc.meta.MTable

import scala.pickling._
import json._

/**
 * Stores a `SocialUser` as a JSON object.
 *
 * This is a hack; it would be better to manually make each field of
 * SocialUser a column, but there are _a lot_ of fields and I'd rather
 * just wait for Slick to make this automatic.
 */
object SocialUserJSONs extends Table[String]("SocialUserJSONs") {
  def json = column[String]("json")

  def * = json
}

object TokenJSONs extends Table[String]("TokenJSONs") {
  def json = column[String]("json")

  def * = json
}

//case class JSONHelper[TableObjectType, ElementType](tableName: String, tableObject: TableObjectType) {
//  def elements = Database.forDataSource(DB.getDataSource()).withSession {
//    if (MTable.getTables(tableName).list().isEmpty) Nil
//    else {
//      val strings = Query(tableObject).list
//      for (string <- stringss) yield string.unpickle[ElementType]
//    }
//  }
//}

/**
 * A persistent user service.
 */
class PersistentUserService(application: Application) extends UserServicePlugin(application) {
  def users: Seq[SocialUser] = Database.forDataSource(DB.getDataSource()).withSession {
    if (MTable.getTables("SocialUserJSONs").list().isEmpty) Nil
    else {
      for (json <- Query(SocialUserJSONs).list) yield json.unpickle[SocialUser]
    }
  }

  def insertUser(user: SocialUser) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("SocialUserJSONs").list().isEmpty) SocialUserJSONs.ddl.create

      SocialUserJSONs.insert(user.pickle.toString)
    }
  }

  def deleteUser(user: SocialUser) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("SocialUserJSONs").list().isEmpty) SocialUserJSONs.ddl.create

      SocialUserJSONs.filter(_.json.toString.unpickle[SocialUser] == user).delete
    }
  }

  def tokens: Seq[Token] = Database.forDataSource(DB.getDataSource()).withSession {
    if (MTable.getTables("TokenJSONs").list().isEmpty) Nil
    else {
      for (json <- Query(TokenJSONs).list) yield json.unpickle[Token]
    }
  }

  def insertToken(user: Token) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("TokenJSONs").list().isEmpty) TokenJSONs.ddl.create

      TokenJSONs.insert(user.pickle.toString)
    }
  }

  def deleteToken(user: Token) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("TokenJSONs").list().isEmpty) TokenJSONs.ddl.create

      TokenJSONs.filter(_.json.toString.unpickle[Token] == user).delete
    }
  }

  def find(id: IdentityId): Option[Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("users = %s".format(users))
    }
    users.find(_.identityId == id)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("users = %s".format(users))
    }

    users.find(u => u.email.map(e => e == email && u.identityId.providerId == providerId).getOrElse(false))
  }

  def save(user: Identity): Identity = user match {
    case socialUser: SocialUser =>
      insertUser(socialUser)
      user
    case _ => sys.error("Identity isn't a SocialUser")
  }

  def save(token: Token) {
    insertToken(token)
  }

  def findToken(uuid: String): Option[Token] = {
    tokens.find(_.uuid == uuid)
  }

  def deleteToken(uuid: String) {
    for (token <- findToken(uuid)) {
      deleteToken(token)
    }
  }

  def deleteTokens() {    
    tokens map (deleteToken)
  }

  def deleteExpiredTokens() {
    for (token <- tokens) {
      if (token.isExpired)
        deleteToken(token)
    }
  }
}
