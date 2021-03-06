package com.zjw.helloworld

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
object HelloWorld {

  final case class Greet(whom: String, replyTo: ActorRef[Greeted])

  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive((context: ActorContext[Greet], message: Greet) => {
    // 收到招呼， 向指定actor发送回复信息
    context.log.info("HelloWorld Actor Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)
    Behaviors.same
  })
}
