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

// Making `id` an Option allows us to use the AutoInc feature.
case class User(id: Option[Long], name: String, email: String, password: String)

object Users extends Table[User]("users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def email = column[String]("email")

  def password = column[String]("password")

  // Every table needs a * projection with the same type as the 
  // table's type parameter.
  def * = (id ?) ~ name ~ email ~ password <> (User.apply _, User.unapply _)
}

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