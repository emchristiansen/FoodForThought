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

import play.api.{ Logger, Application }
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId

import scalatestextra._

import scala.pickling._
import scala.pickling.binary._

import scala.slick.driver.SQLiteDriver.simple._

/**
 * A Sample In Memory user service in Scala
 *
 * IMPORTANT: This is just a sample and not suitable for a production environment since
 * it stores everything in memory.
 */
class PersistentUserService(application: Application) extends UserServicePlugin(application) {
//  val database = Database.forURL("jdbc:sqlite:db.sqlite", driver = "org.sqlite.JDBC")
  val database = Database.forURL("jdbc:mariadb://localhost:3306/FFTSite", driver = "org.mariadb.jdbc.Driver")

  private val users =
    PersistentMap.connectElseCreate[String, Identity]("users", database)
  private val tokens =
    PersistentMap.connectElseCreate[String, Token]("tokens", database)

  def debugUsersAndTokens(label: String) {
    Logger.debug(s"label = $label")
    Logger.debug(s"users = ${users.toMap.toString}")
    Logger.debug(s"tokens = ${tokens.toMap.toString}")
  }

  //  private val users = collection.mutable.Map[String, Identity]()
  //  private val tokens = collection.mutable.Map[String, Token]()

  def find(id: IdentityId): Option[Identity] = {
    debugUsersAndTokens("find")
    
    users.get(id.userId + id.providerId)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    debugUsersAndTokens("findByEmailAndProvider")
    
    users.values.find(u => u.email.map(e => e == email && u.identityId.providerId == providerId).getOrElse(false))
  }

  def save(user: Identity): Identity = {
    debugUsersAndTokens("save Identity")

    users += (user.identityId.userId + user.identityId.providerId -> user)

    // this sample returns the same user object, but you could return an instance of your own class
    // here as long as it implements the Identity trait. This will allow you to use your own class in the protected
    // actions and event callbacks. The same goes for the find(id: UserId) method.
    user
  }

  def save(token: Token) {
    debugUsersAndTokens("save Token")
    
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    debugUsersAndTokens("findToken")
    
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    debugUsersAndTokens("deleteToken")
    
    tokens -= uuid
  }

  def deleteTokens() {
    debugUsersAndTokens("deleteTokens")
    
    tokens.clear()
  }

  def deleteExpiredTokens() {
    debugUsersAndTokens("debugUsersAndTokens")
    
    tokens.filter(_._2.isExpired).keys map { token =>
      tokens -= token
    }
  }
}
