package com.zjw.store

import java.util.concurrent.ConcurrentHashMap

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.jdk.CollectionConverters._

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
object Store {
  def apply(): Behavior[Command] =
    Behaviors.setup(context => new Store(context))

  // 所有命令的父类
  sealed trait Command

  final case class SetElement(
                               requestId: String,
                               key: String,
                               value: Object,
                               replyTo: ActorRef[ResponseSetElement]
                             ) extends Command

  final case class ResponseSetElement(requestId: String, result: String)


  final case class GetElement(
                               requestId: String,
                               key: String,
                               replyTo: ActorRef[ResponseGetElement]
                             ) extends Command

  final case class ResponseGetElement(requestId: String, result: Option[Object])

  final case class GetAllElement(
                               requestId: String,
                               replyTo: ActorRef[ResponseGetAllElement]
                             ) extends Command

  final case class ResponseGetAllElement(requestId: String, result: Map[String, Object])
}


class Store(context: ActorContext[Store.Command]) extends AbstractBehavior[Store.Command](context) {
  val cache = new ConcurrentHashMap[String, Object]()

  override def onMessage(msg: Store.Command): Behavior[Store.Command] = {
    msg match {
      case Store.SetElement(requestId, key, value, replyTo) =>
        this.cache.put(key, value)
        replyTo ! Store.ResponseSetElement(requestId, "success")
        this
      case Store.GetElement(requestId, key, replyTo) =>
        val retElement = this.cache.get(key)
        val result: Option[Object] = Option(retElement)
        replyTo ! Store.ResponseGetElement(requestId, result)

        this
      case Store.GetAllElement(requestId, replyTo) =>
        replyTo ! Store.ResponseGetAllElement(requestId, this.cache.asScala.toMap)
        this
    }

  }
}
