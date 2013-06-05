package controllers

import play.api._
import play.api.mvc._
import play.api.templates._

object Application extends Controller {
  //  def markdownToHTML = Action {
  //    val asset = controllers.Assets.at(path="/public/markdown", "index.md")
  //    
  //    Html(new org.pegdown.PegDownProcessor().markdownToHtml(asset))
  //  }

  def markdownToHTML(markdown: String) =
    Html(new org.pegdown.PegDownProcessor().markdownToHtml(markdown))

  def index = Action { Ok(views.html.index()) }

  def methods = Action { Ok(views.html.methods()) }

  def results = Action { Ok(views.html.results()) }

  def blog = Action { Redirect("http://so3fft.blogspot.com") }
                                                                                             ˜
  def group = Action {
    Redirect("https://groups.google.com/forum/?fromgroups#!forum/so3foodforthought")
  }

  def source = Action {
    Redirect("https://github.com/emchristiansen/FoodForThought")
  }
}