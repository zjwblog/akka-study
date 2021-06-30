package com.zjw.interaction

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.ActorSystem
import akka.pattern.StatusReply
import akka.util.Timeout
import com.zjw.interaction.CookieFabric.GiveMeCookies

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/30
 */
object Main {
  def main(args: Array[String]): Unit = {
    val cookieFabric = ActorSystem[GiveMeCookies](CookieFabric(), "CookieFabric")
    implicit val timeout: Timeout = 3.seconds
    implicit val system = cookieFabric
    val result: Future[CookieFabric.Cookies] = cookieFabric.askWithStatus(ref => CookieFabric.GiveMeCookies(3, ref))

    implicit val ec = cookieFabric.executionContext

    result.onComplete {
      case Success(CookieFabric.Cookies(count)) => println(s"Yay, $count cookies!")
      case Failure(StatusReply.ErrorMessage(reason)) => println(s"No cookies for me. $reason")
      case Failure(ex) => println(s"Boo! didn't get cookies: ${ex.getMessage}")
    }
  }
}
