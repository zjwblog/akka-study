package com.zjw.first.supervising

import akka.actor.typed.{Behavior, PostStop, PreRestart, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object SupervisedActor {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new SupervisedActor(context))
}

class SupervisedActor(context: ActorContext[String]) extends AbstractBehavior[String](context) {
  println("supervised actor started")

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "fail" =>
        println("supervised actor fails now")
        throw new Exception("I failed!")
    }

  override def onSignal: PartialFunction[Signal, Behavior[String]] = {
    case PreRestart => // 启动前会收到这个信号
      println("supervised actor will be restarted")
      this
    case PostStop => // 介绍之后会处理这个信号
      println("supervised actor stopped")
      this
  }

}
