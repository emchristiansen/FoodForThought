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
case class SocialUserJSON(uniqueID: Option[Long], json: String)

object SocialUserJSONs extends Table[SocialUserJSON]("SocialUserJSONs") {
  def uniqueID = column[Long]("uniqueID", O.PrimaryKey, O.AutoInc)

  def json = column[String]("json")

  def * = (uniqueID ?) ~ json <> (SocialUserJSON.apply _, SocialUserJSON.unapply _)
}

case class TokenJSON(uniqueID: Option[Long], json: String)

object TokenJSONs extends Table[TokenJSON]("TokenJSONs") {
  def uniqueID = column[Long]("uniqueID", O.PrimaryKey, O.AutoInc)

  def json = column[String]("json")

  def * = (uniqueID ?) ~ json <> (TokenJSON.apply _, TokenJSON.unapply _)
}

/**
 * A persistent user service.
 */
class PersistentUserService(application: Application) extends UserServicePlugin(application) {
  //  private var tokens = Map[String, Token]()

  def users: Seq[SocialUser] = Database.forDataSource(DB.getDataSource()).withSession {
    if (MTable.getTables("SocialUserJSONs").list().isEmpty) Nil
    else {
      val jsons = Query(SocialUserJSONs).list
      for (SocialUserJSON(_, json) <- jsons) yield json.unpickle[SocialUser]
    }
  }

  def addUser(user: SocialUser) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("SocialUserJSONs").list().isEmpty) SocialUserJSONs.ddl.create

      SocialUserJSONs.insert(SocialUserJSON(None, user.pickle.toString))
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
      addUser(socialUser)
      user
    case _ => sys.error("Identity isn't a SocialUser")
  }

  def save(token: Token) {
    Database.forDataSource(DB.getDataSource()).withSession {
      if (MTable.getTables("TokenJSONs").list().isEmpty) TokenJSONs.ddl.create
      
      TokenJSONs.insert(TokenJSON(None, token.pickle.toString))
    }
  }

  def findToken(token: String): Option[Token] = {

    tokens.get(token)
  }

  def deleteToken(uuid: String) {

    tokens -= uuid
  }

  def deleteTokens() {

    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }
}
