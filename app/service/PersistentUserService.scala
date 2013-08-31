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

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.templates.Html
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.jdbc.meta.MTable

//case class Foo(a: Int, b: String)
//
//object Foos extends Table[Foo]("foos") {
//  def a = column[Int]("a", O.PrimaryKey)
//  
//  def b = column[String]("b")
//  
//  def * = a ~ b <> (Foo.apply _, Foo.unapply _)
//}
//
//import scala.slick.lifted.MappedTypeMapper
//import scala.slick.lifted.TypeMapper
//
//object Grr {
//implicit val FooTupleIntStringMapper: TypeMapper[Foo] = 
//  MappedTypeMapper.base[Foo, (Int, String)](
//    d => ???, 
//    t => ???)
//}
//
//case class Bar(f: Foo, c: Double)
//
//object Bars extends Table[Bar]("bars") {
//  def f = column[Foo]("f", O.PrimaryKey)
//  
//  def c = column[Double]("c")
//  
//  def * = f ~ c <> (Bar.apply _, Bar.unapply _)
//}

object SocialUsers extends Table[SocialUser]("SocialUsers") {
  def uniqueID = column[Long]("uniqueID", O.PrimaryKey, O.AutoInc)

  def identityID_userID = column[String]("identityID_userID")
  def identityID_providerID = column[String]("identityID_providerID")

  def firstName = column[String]("firstName")

  def lastName = column[String]("lastName")

  def fullName = column[String]("fullName")

  def email = column[String]("email")

  def avatarURL = column[String]("avatarURL")

  def authMethod_method = column[String]("authMethod_method")

  def oAuth1Info_token = column[String]("oAuth1Info_token")
  def oAuth1Info_secret = column[String]("oAuth1Info_secret")

  def oAuth2Info_accessToken = column[String]("oAuth2Info_accessToken")
  def oAuth2Info_tokenType = column[String]("oAuth2Info_tokenType")
  def oAuth2Info_expiresIn = column[Int]("oAuth2Info_expiresIn")
  def oAuth2Info_refreshToken = column[String]("oAuth2Info_refreshToken")

  def passwordInfo_hasher = column[String]("passwordInfo_hasher")
  def passwordInfo_password = column[String]("passwordInfo_password")
  def passwordInfo_salt = column[String]("passwordInfo_salt")
  
  def * = (uniqueID ?) ~ identityID_userID ~ identityID_providerID ~ firstName ~ lastName ~ fullName ~
  email ~ (avatarURL ?) ~ authMethod_method ~ oAuth1Info_token ~ oAuth1Info_secret ~ 
  oAuth2Info_accessToken ~ (oAuth2Info_tokenType ?) ~ (oAuth2Info_expiresIn ?) ~ (oAuth2Info_refreshToken ?) ~
  passwordInfo_hasher ~ passwordInfo_password ~ (passwordInfo_salt ?) <> (packSocialUser _, unpackSocialUser _)
  
  def packSocialUser(
    iiui: String,
    iipi: String,
    fn: String,
    ln: String,
    fulln: String,
    e: String,
    au: Option[String],
    amm: String,
    oa1t: String,
    oa1s: String,
    oa2at: String,
    oa2tt: Option[String],
    oa2ei: Option[Int],
    oa2rt: Option[String],
    pih: String,
    pip: String,
    pis: Option[String]): SocialUser = ???
    
  def unpackSocialUser(socialUser: SocialUser): (
      String, 
      String, 
      String,
      String,
      String,
      String,
      Option[String],
      String,
      String,
      String,
      String,
      Option[String],
      Option[Int],
      Option[String],
      String,
      String,
      Option[String]) = ???

  // Every table needs a * projection with the same type as the 
  // table's type parameter.
}

case class IdentityId(userId: String, providerId: String)

case class SocialUser(identityId: IdentityId, firstName: String, lastName: String, fullName: String, email: Option[String],
  avatarUrl: Option[String], authMethod: AuthenticationMethod,
  oAuth1Info: Option[OAuth1Info] = None,
  oAuth2Info: Option[OAuth2Info] = None,
  passwordInfo: Option[PasswordInfo] = None)

case class AuthenticationMethod(method: String)

case class OAuth1Info(token: String, secret: String)

case class OAuth2Info(accessToken: String, tokenType: Option[String] = None,
  expiresIn: Option[Int] = None, refreshToken: Option[String] = None)

case class PasswordInfo(hasher: String, password: String, salt: Option[String] = None)

/**
 * A Sample In Memory user service in Scala
 *
 * IMPORTANT: This is just a sample and not suitable for a production environment since
 * it stores everything in memory.
 */
class PersistentUserService(application: Application) extends UserServicePlugin(application) {
  private var users = Map[String, Identity]()
  private var tokens = Map[String, Token]()

  def find(id: IdentityId): Option[Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("users = %s".format(users))
    }
    users.get(id.userId + id.providerId)
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("users = %s".format(users))
    }
    users.values.find(u => u.email.map(e => e == email && u.identityId.providerId == providerId).getOrElse(false))
  }

  def save(user: Identity): Identity = {
    users = users + (user.identityId.userId + user.identityId.providerId -> user)
    // this sample returns the same user object, but you could return an instance of your own class
    // here as long as it implements the Identity trait. This will allow you to use your own class in the protected
    // actions and event callbacks. The same goes for the find(id: UserId) method.
    user
  }

  def save(token: Token) {
    tokens += (token.uuid -> token)
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
