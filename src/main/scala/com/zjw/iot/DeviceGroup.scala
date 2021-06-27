package com.zjw.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.concurrent.duration._

object DeviceGroup {
  def apply(groupId: String): Behavior[Command] =
    Behaviors.setup(context => new DeviceGroup(context, groupId))

  trait Command

  private final case class DeviceTerminated(device: ActorRef[Device.Command], groupId: String, deviceId: String) extends Command

}

class DeviceGroup(context: ActorContext[DeviceGroup.Command], groupId: String)
  extends AbstractBehavior[DeviceGroup.Command](context) {

  import DeviceGroup._
  import DeviceManager.RequestAllTemperatures

  private var deviceIdToActor = Map.empty[String, ActorRef[Device.Command]]

  context.log.info("DeviceGroup {} started", groupId)

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case RequestAllTemperatures(requestId, gId, replyTo) =>
        if (gId == groupId) {
          context.spawnAnonymous(DeviceGroupQuery(deviceIdToActor, requestId = requestId, requester = replyTo, 3.seconds))
          this
        } else
          Behaviors.unhandled
    }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info("DeviceGroup {} stopped", groupId)
      this
  }
}
