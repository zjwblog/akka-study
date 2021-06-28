package com.zjw.iot

import scala.concurrent.duration._
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import com.zjw.iot.Device.Command
import com.zjw.iot.DeviceGroupQuery.WrappedRespondTemperature
import com.zjw.iot.DeviceManager.DeviceNotAvailable
import com.zjw.iot.DeviceManager.DeviceTimedOut
import com.zjw.iot.DeviceManager.RespondAllTemperatures
import com.zjw.iot.DeviceManager.Temperature
import com.zjw.iot.DeviceManager.TemperatureNotAvailable

class DeviceGroupQuerySpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "DeviceGroupQuery Actor" must {

    //#query-test-normal
    "向DeviceGroupQuery记录一批设备的温度，当记录完成之后返回所有设备的温度信息" in {
      val requester = createTestProbe[RespondAllTemperatures]()
      // 组织一批设备
      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()
      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)
      // 生成一个DeviceGroupQuery Actor，并告诉DeviceGroupQuery，有设备1和设备2需要收集温度信息
      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))
      // 可以设置设备Actor的返回值类型
      // device1.expectMessageType[Device.ReadTemperature]
      // device2.expectMessageType[Device.ReadTemperature]
      // 向DeviceGroupQuery中记录两个设备的温度信息，之后当所有设备的温度信息收集完成才会返回RespondAllTemperatures
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))
      //
      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))))
    }

    //#query-test-no-reading
    "向DeviceGroupQuery记录一批设备的温度，当记录完成之后返回所有设备的温度信，并模拟存在设备的温度信息为None的情况" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", None))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> TemperatureNotAvailable, "device2" -> Temperature(2.0))))
    }

    "向DeviceGroupQuery记录一批设备的温度，当在查询之前设备下线" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(2.0)))
      device2.stop()

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(2.0), "device2" -> DeviceNotAvailable)))
    }

    "return temperature reading even if device stops after answering" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor = spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 3.seconds))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device2", Some(2.0)))

      device2.stop()

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0))))
    }

    "当收集设备温度超时，返回超时信息" in {
      val requester = createTestProbe[RespondAllTemperatures]()

      val device1 = createTestProbe[Command]()
      val device2 = createTestProbe[Command]()

      val deviceIdToActor = Map("device1" -> device1.ref, "device2" -> device2.ref)

      val queryActor =
        spawn(DeviceGroupQuery(deviceIdToActor, requestId = 1, requester = requester.ref, timeout = 200.millis))

      device1.expectMessageType[Device.ReadTemperature]
      device2.expectMessageType[Device.ReadTemperature]

      queryActor ! WrappedRespondTemperature(Device.RespondTemperature(requestId = 0, "device1", Some(1.0)))
      // device2温度信息不记录，等待超时

      requester.expectMessage(
        RespondAllTemperatures(
          requestId = 1,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> DeviceTimedOut)))
    }
  }

}
