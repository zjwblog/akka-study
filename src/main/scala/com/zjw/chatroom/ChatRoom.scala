package com.zjw.chatroom

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
object ChatRoom {
  sealed trait RoomCommand
  // 建立Session链接请求类
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  sealed trait SessionCommand
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand
  private final case class PublishSessionMessage(screenName: String, message: String) extends RoomCommand

  def apply(): Behavior[RoomCommand] = chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case GetSession(screenName, client) =>
          // 创建一个Session，用来和 client交互
          val ses = context.spawn(session(context.self, screenName, client), URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
          // 将创建的Session通知到Client
          client ! SessionGranted(ses)
          // 保存当前session到
          chatRoom(ses :: sessions)
        case PublishSessionMessage(screenName, message) =>
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.stopped
      }
    }

  private def session(
                       room: ActorRef[PublishSessionMessage],
                       screenName: String,
                       client: ActorRef[SessionEvent]
                     ): Behavior[SessionCommand] =
    Behaviors.receiveMessage {
      case PostMessage(message) =>
        room ! PublishSessionMessage(screenName, message)
        Behaviors.same
      case NotifyClient(message) =>
        client ! message
        Behaviors.same
    }
}
