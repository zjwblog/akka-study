package com.zjw.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}

object DeviceManager {
  def apply(): Behavior[Command] = Behaviors.setup(context => new DeviceManager(context))

  sealed trait Command

  sealed trait TemperatureReading

  // 向DeviceManager注册设备，需要给定组ID和设备ID和设备的引用
  final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered]) extends DeviceManager.Command with DeviceGroup.Command

  // 注册成功之后的返回数据类型
  final case class DeviceRegistered(device: ActorRef[Device.Command])

  // 向DeviceManager请求指定的设备组，要求返回能够返回ReplyDeviceList的引用
  final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList]) extends DeviceManager.Command with DeviceGroup.Command

  // 包装设备ID列表的对象
  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  // 请求注销设备组的请求体
  private final case class DeviceGroupTerminated(groupId: String) extends DeviceManager.Command

  // 请求返回能够获取指定设备组的所有温度的引用
  final case class RequestAllTemperatures(requestId: Long, groupId: String, replyTo: ActorRef[RespondAllTemperatures]) extends DeviceGroupQuery.Command with DeviceGroup.Command with DeviceManager.Command

  // 返回所有温度的包装类
  final case class RespondAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])

  // 包装温度的类
  final case class Temperature(value: Double) extends TemperatureReading

  // 温度暂时不可用的类
  case object TemperatureNotAvailable extends TemperatureReading

  // 设备不可用的类
  case object DeviceNotAvailable extends TemperatureReading

  // 设备文档获取失败的类
  case object DeviceTimedOut extends TemperatureReading

}

class DeviceManager(context: ActorContext[DeviceManager.Command]) extends AbstractBehavior[DeviceManager.Command](context) {

  import DeviceManager._

  var groupIdToActor = Map.empty[String, ActorRef[DeviceGroup.Command]]

  context.log.info("DeviceManager started")

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case trackMsg@RequestTrackDevice(groupId, _, replyTo) =>
        groupIdToActor.get(groupId) match {
          case Some(ref) =>
            ref ! trackMsg
          case None =>
            context.log.info("Creating device group actor for {}", groupId)
            val groupActor = context.spawn(DeviceGroup(groupId), "group-" + groupId)
            context.watchWith(groupActor, DeviceGroupTerminated(groupId))
            groupActor ! trackMsg
            groupIdToActor += groupId -> groupActor
        }
        this
      // 返回所有给定的设备组引用
      case req@RequestDeviceList(requestId, groupId, replyTo) =>
        groupIdToActor.get(groupId) match {
          // 如果有这个设备组则返回设备组引用
          case Some(ref) =>
            ref ! req
          // 如果没有这个设备组则... TODO
          case None =>
            replyTo ! ReplyDeviceList(requestId, Set.empty)
        }
        this
      // 删除给定的设备组ID删除设备组
      case DeviceGroupTerminated(groupId) =>
        context.log.info("Device group actor for {} has been terminated", groupId)
        groupIdToActor -= groupId
        this
    }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info("DeviceManager stopped")
      this
  }

}
