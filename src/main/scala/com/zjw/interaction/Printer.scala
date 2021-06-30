package com.zjw.interaction

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/30
 */
object Printer {
  case class PrintMe(message: String)

  def apply(): Behavior[PrintMe] =
    Behaviors.receive {
      case (context, PrintMe(message)) =>
        context.log.info(message)
        Behaviors.same
    }
}
