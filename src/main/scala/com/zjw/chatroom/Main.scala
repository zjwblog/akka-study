package com.zjw.chatroom

import akka.actor.typed.{ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.NotUsed

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      // 创建一个 ChatRoom Actor
      val chatRoom = context.spawn(ChatRoom(), "chatroom")
      // 创建一个 Gabbler Actor
      val gabblerRef = context.spawn(Gabbler(), "gabbler")
//      val gabblerRef2 = context.spawn(Gabbler(), "gabbler")

      // 向ChatRoom获取一个Session,链接到Gabbler
      chatRoom ! ChatRoom.GetSession("Gabbler A", gabblerRef)
//      chatRoom ! ChatRoom.GetSession("Gabbler B", gabblerRef2)

      Behaviors.same
    }

  def main(args: Array[String]): Unit = {
    ActorSystem(Main(), "ChatRoomDemo")
  }

}
