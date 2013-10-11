package fftsite.controllers

import fftsite._
import securesocial.controllers.TemplatesPlugin
import play.api.mvc.{ RequestHeader, Request }
import play.api.templates.{ Html, Txt }
import play.api.{ Logger, Plugin, Application }
import securesocial.core.{ Identity, SecuredRequest, SocialUser }
import play.api.data.Form
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo

/**
 * The default views plugin.  If you need to customise the views just create a new plugin that
 * extends TemplatesPlugin and register it in the play.plugins file instead of this one.
 *
 * @param application
 */
class CustomTemplatesPlugin(application: Application) extends TemplatesPlugin {
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
    msg: Option[String] = None): Html =
    {
      views.html.customss.login(form, msg)
    }

  override def getSignUpPage[A](
    implicit request: Request[A],
    form: Form[RegistrationInfo],
    token: String): Html = {
    views.html.customss.Registration.signUp(form, token)
  }

  override def getStartSignUpPage[A](
    implicit request: Request[A],
    form: Form[String]): Html = {
    views.html.customss.Registration.startSignUp(form)
  }

  override def getStartResetPasswordPage[A](
    implicit request: Request[A],
    form: Form[String]): Html = {
    views.html.customss.Registration.startResetPassword(form)
  }

  def getResetPasswordPage[A](
    implicit request: Request[A],
    form: Form[(String, String)],
    token: String): Html = {
    views.html.customss.Registration.resetPasswordPage(form, token)
  }

  def getPasswordChangePage[A](
    implicit request: SecuredRequest[A],
    form: Form[ChangeInfo]): Html = {
    views.html.customss.passwordChange(form)
  }

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.customss.notAuthorized()
  }

  def getSignUpEmail(
    token: String)(
      implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.signUpEmail(token)))
  }

  def getAlreadyRegisteredEmail(
    user: Identity)(
      implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.alreadyRegisteredEmail(user)))
  }

  def getWelcomeEmail(
    user: Identity)(
      implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.welcomeEmail(user)))
  }

  def getUnknownEmailNotice()(
    implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.unknownEmailNotice(request)))
  }

  def getSendPasswordResetEmail(
    user: Identity,
    token: String)(
      implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.passwordResetEmail(user, token)))
  }

  def getPasswordChangedNoticeEmail(
    user: Identity)(
      implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.customss.mails.passwordChangedNotice(user)))
  }
}