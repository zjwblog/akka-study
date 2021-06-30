package com.zjw.interaction

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/30
 */
object Hal {

  sealed trait Command

  case class OpenThePodBayDoorsPlease(replyTo: ActorRef[Response]) extends Command

  case class Response(message: String)

  def apply(): Behaviors.Receive[Hal.Command] =
    Behaviors.receiveMessage[Command] {
      case OpenThePodBayDoorsPlease(replyTo) =>
        replyTo ! Response("I'm sorry, Dave. I'm afraid I can't do that.")
        Behaviors.same
    }
}
