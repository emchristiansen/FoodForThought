package fftsite.controllers

import fftsite._

import org.joda.time._
import fftsite.models._

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.templates.Html
import java.io.File
import play.api.data._
import play.api.data.Forms._
import play.api.db._
import play.api.Play.current
import slick.driver.H2Driver.simple._
import slick.driver.H2Driver.simple.Database.threadLocalSession
import scala.slick.jdbc.meta.MTable
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import securesocial.core._

object FFTSite extends Controller with securesocial.core.SecureSocial {
  val updateSaved = "fftUpdateSaved"

  def loadResourceAsString(resource: String): String = {
    val file = getClass.getResource(resource).getFile
    io.Source.fromFile(file).mkString
  }

  def markdownToHTML(markdown: String) =
    Html(new org.pegdown.PegDownProcessor().markdownToHtml(markdown))

  def resourceMarkdownToHTML = (markdownToHTML _) compose loadResourceAsString

  def getIndex = Action { implicit request => Ok(views.html.index()) }

  def getMethods = Action { implicit request => Ok(views.html.methods()) }

  def getResults = Action { implicit request => Ok(views.html.results()) }

  def getFAQ = Action { implicit request => Ok(views.html.faq()) }

  def getBlog = Action { implicit request => Redirect("http://so3fft.blogspot.com") }

  def getGroup = Action { implicit request =>
    Redirect("https://groups.google.com/forum/?fromgroups#!forum/so3foodforthought")
  }

  def getSource = Action { implicit request =>
    Redirect("https://github.com/emchristiansen/FoodForThought")
  }

  def getAuthenticateDropdown = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) => Ok(views.html.accountAuthenticated(user.firstName))
      case None => Ok(views.html.accountNotAuthenticated())
    }
  }

  // TODO: Delete this.
  def testID = Models.users(IdentityId("echristiansen@eng.ucsd.edu", "userpass"))

  //  val userInformationForm = Form(mapping(
  //    "studentID" -> optional(text),
  //    "employeeID" -> optional(text))(UserInformation.apply)(UserInformation.unapply))
  //
  //  val dietaryInformationForm = Form(mapping(
  //    "restrictions" -> optional(text),
  //    "preferences" -> optional(text),
  //    "additionalNotes" -> optional(text))(DietaryInformation.apply)(DietaryInformation.unapply))

  val profileForm = Form(tuple(
    "userInformation" -> mapping(
      "studentID" -> optional(text),
      "employeeID" -> optional(text))(UserInformation.apply)(UserInformation.unapply),
    "dietaryInformation" -> mapping(
      "restrictions" -> optional(text),
      "preferences" -> optional(text),
      "additionalNotes" -> optional(text))(DietaryInformation.apply)(DietaryInformation.unapply)))

  // TODO: Change to SecuredAction
  def getProfile = UserAwareAction { implicit request =>
    val user: SocialUser = testID

    val userInformation = Models.userInformation.getOrElse(
      user.identityId,
      UserInformation(None, None))

    val dietaryInformation = Models.dietaryInformation.getOrElse(
      user.identityId,
      DietaryInformation(None, None, None))

    Ok(views.html.profile(profileForm.fill((userInformation, dietaryInformation))))
  }

  // TODO: Add flashing.
  def postProfile = UserAwareAction { implicit request =>
    val user: SocialUser = testID

    profileForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.profile(formWithErrors)),
      value => {
        Models.userInformation(user.identityId) = value._1
        Models.dietaryInformation(user.identityId) = value._2
        Redirect(fftsite.controllers.routes.FFTSite.getProfile)
      })
  }

  val signUpForm = Form(
    "freshFood" -> list(boolean))

  def getSignUp = UserAwareAction { implicit request =>
    val user: SocialUser = testID

    def nextDays(
      roster: Map[String, IdentityId],
      numDays: Int): List[(LocalDate, IdentityId)] = ???

    Ok(views.html.signUp(signUpForm.fill(List(false, false, false))))

  }
  
  def postSignUp = UserAwareAction { implicit request =>
    val user: SocialUser = testID

    ???
  }
}