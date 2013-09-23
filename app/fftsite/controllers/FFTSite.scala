package fftsite.controllers

import fftsite._

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


object FFTSite extends Controller with securesocial.core.SecureSocial {
  def loadResourceAsString(resource: String): String = {
    val file = getClass.getResource(resource).getFile
    io.Source.fromFile(file).mkString
  }

  def markdownToHTML(markdown: String) =
    Html(new org.pegdown.PegDownProcessor().markdownToHtml(markdown))

  def resourceMarkdownToHTML = (markdownToHTML _) compose loadResourceAsString

  def getIndex = Action { Ok(views.html.index()) }

  def getMethods = Action { Ok(views.html.methods()) }

  def getResults = Action { Ok(views.html.results()) }

  def getFAQ = Action { Ok(views.html.faq()) }

  def getBlog = Action { Redirect("http://so3fft.blogspot.com") }

  def getGroup = Action {
    Redirect("https://groups.google.com/forum/?fromgroups#!forum/so3foodforthought")
  }

  def getSource = Action {
    Redirect("https://github.com/emchristiansen/FoodForThought")
  }

  def getAuthenticateDropdown = UserAwareAction { implicit request =>
    request.user match {
      case Some(user) => Ok(views.html.accountAuthenticated(user.firstName))
      case None => Ok(views.html.accountNotAuthenticated())
    }
  }

//  val myForm = Form("age" -> number)
//
//  def getTest = Action { implicit request =>
//    Ok(views.html.test(myForm))
////    Redirect(routes.Application.getIndex)
//  }
//
//  def postTest = Action {
//    Redirect(routes.FFTSite.getIndex)
//  }
}