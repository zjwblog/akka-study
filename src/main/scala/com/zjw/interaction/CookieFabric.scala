package com.zjw.interaction

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/30
 */
object CookieFabric {

  sealed trait Command
  case class GiveMeCookies(count: Int, replyTo: ActorRef[StatusReply[Cookies]]) extends Command
  case class Cookies(count: Int)

  def apply(): Behaviors.Receive[CookieFabric.GiveMeCookies] =
    Behaviors.receiveMessage { message =>
      if (message.count >= 5)
        message.replyTo ! StatusReply.Error("Too many cookies.")
      else
        message.replyTo ! StatusReply.Success(Cookies(message.count))
      Behaviors.same
    }
}
