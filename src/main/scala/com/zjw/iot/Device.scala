package com.zjw.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}

object Device {
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new Device(context, groupId, deviceId))

  // 所有命令的父类
  sealed trait Command

  // 读取温度信息的请求的消息协议
  final case class ReadTemperature(requestId: Long, replyTo: ActorRef[RespondTemperature]) extends Command

  // 响应读取温度请求的消息协议
  final case class RespondTemperature(requestId: Long, deviceId: String, value: Option[Double])

  // 存入温度信息的消息协议
  final case class RecordTemperature(requestId: Long, value: Double, replyTo: ActorRef[TemperatureRecorded]) extends Command

  // 响应存入温度请求的消息协议
  final case class TemperatureRecorded(requestId: Long)

  case object Passivate extends Command
}

class Device(context: ActorContext[Device.Command], groupId: String, deviceId: String)
  extends AbstractBehavior[Device.Command](context) {

  import Device._

  // 当前温度信息
  var lastTemperatureReading: Option[Double] = None

  context.log.info2("Device actor {}-{} started", groupId, deviceId)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      // 处理写入温度请求
      case RecordTemperature(id, value, replyTo) =>
        context.log.info2("Recorded temperature reading {} with {}", value, id)
        lastTemperatureReading = Some(value)
        replyTo ! TemperatureRecorded(id)
        this
      // 处理读取温度请求
      case ReadTemperature(id, replyTo) =>
        replyTo ! RespondTemperature(id, deviceId, lastTemperatureReading)
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info2("Device actor {}-{} stopped", groupId, deviceId)
      this
  }

}
