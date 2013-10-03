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
package fftsite.controllers

import _root_.java.util.UUID
import play.api.mvc.{ Result, Action, Controller }
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.{ Play, Logger }
import securesocial.core.providers.UsernamePasswordProvider
import securesocial.core._
import com.typesafe.plugin._
import Play.current
import securesocial.core.providers.utils._
import org.joda.time.DateTime
import play.api.i18n.Messages
import securesocial.core.providers.Token
import scala.Some
import securesocial.core.IdentityId

/**
 * A copy of `securesocial.controllers.Registration` which allows us
 * to use a custom email verifier.
 *
 */
object CustomRegistration extends Controller {
  import securesocial.controllers._
  import securesocial.controllers.Registration._

  val customEmail: Mapping[String] = email verifying (nonEmpty) verifying (
    "Address must be @cs.ucsd.edu, @eng.ucsd.edu, or @ucsd.edu",
    e =>
      e.contains("@cs.ucsd.edu") ||
        e.contains("@eng.ucsd.edu") ||
        e.contains("@ucsd.edu"))

  val startForm = Form(
    Email -> customEmail)

  private def createToken(email: String, isSignUp: Boolean): (String, Token) = {
    val uuid = UUID.randomUUID().toString
    val now = DateTime.now

    val token = Token(
      uuid, email,
      now,
      now.plusMinutes(TokenDuration),
      isSignUp = isSignUp)
    UserService.save(token)
    (uuid, token)
  }

  /**
   * Starts the sign up process
   */
  def startSignUp = Action { implicit request =>
    if (SecureSocial.enableRefererAsOriginalUrl) {
      SecureSocial.withRefererAsOriginalUrl(Ok(use[TemplatesPlugin].getStartSignUpPage(request, startForm)))
    } else {
      Ok(use[TemplatesPlugin].getStartSignUpPage(request, startForm))
    }
  }

  def handleStartSignUp = Action { implicit request =>
    startForm.bindFromRequest.fold(
      errors => {
        BadRequest(use[TemplatesPlugin].getStartSignUpPage(request, errors))
      },
      email => {
        // check if there is already an account for this email address
        UserService.findByEmailAndProvider(email, UsernamePasswordProvider.UsernamePassword) match {
          case Some(user) => {
            // user signed up already, send an email offering to login/recover password
            Mailer.sendAlreadyRegisteredEmail(user)
          }
          case None => {
            val token = createToken(email, isSignUp = true)
            Mailer.sendSignUpEmail(email, token._1)
          }
        }
        Redirect(onHandleStartSignUpGoTo).flashing(Success -> Messages(ThankYouCheckEmail), Email -> email)
      })
  }

}
