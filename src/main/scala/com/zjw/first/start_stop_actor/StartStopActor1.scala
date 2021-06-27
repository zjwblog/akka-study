package com.zjw.first.start_stop_actor

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object StartStopActor1 {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new StartStopActor1(context))
}

class StartStopActor1(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  println("first started")
  val second = context.spawn(StartStopActor2(), "second")

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "stop" => Behaviors.stopped
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PostStop =>
      println(s"second: ${second.path}")
      println("first stopped")
      this
  }

}