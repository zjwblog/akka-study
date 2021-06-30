package com.zjw.interaction

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/30
 */
object Dave {

  sealed trait Command
  private case class AdaptedResponse(message: String) extends Command

  def apply(hal: ActorRef[Hal.Command]): Behavior[Dave.Command] =
    Behaviors.setup[Command] { context =>
      implicit val timeout: Timeout = 3.seconds

      context.ask(hal, Hal.OpenThePodBayDoorsPlease) {
        case Success(Hal.Response(message)) => AdaptedResponse(message)
        case Failure(_) => AdaptedResponse("Request failed")
      }

      val requestId = 1
      context.ask(hal, Hal.OpenThePodBayDoorsPlease) {
        case Success(Hal.Response(message)) => AdaptedResponse(s"$requestId: $message")
        case Failure(_) => AdaptedResponse(s"$requestId: Request failed")
      }

      Behaviors.receiveMessage {
        case AdaptedResponse(message) =>
          context.log.info("Got response from hal: {}", message)
          Behaviors.same
      }
    }
}
