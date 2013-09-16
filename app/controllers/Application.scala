package controllers

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

object Application extends Controller {
  //  def markdownToHTML = Action {
  //    val asset = controllers.Assets.at(path="/public/markdown", "index.md")
  //    
  //    Html(new org.pegdown.PegDownProcessor().markdownToHtml(asset))
  //  }

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


}