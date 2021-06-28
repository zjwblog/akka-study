package com.zjw.iot

import scala.concurrent.duration._
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike
import com.zjw.iot.Device._
import com.zjw.iot.DeviceManager._

class DeviceGroupSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "DeviceGroup Actor" must {
    "注册设备Actor" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 向Group注册第一个设备Actor
      groupActor ! RequestTrackDevice("group", "device1", probe.ref)
      val registered1 = probe.receiveMessage()
      val deviceActor1 = registered1.device
      // 向Group注册第二个设备Actor
      groupActor ! RequestTrackDevice("group", "device2", probe.ref)
      val registered2 = probe.receiveMessage()
      val deviceActor2 = registered2.device
      // 判断两个返回的设备Actor不是同一个
      deviceActor1 should !==(deviceActor2)

      // 分别向两个设备Actor记录温度信息
      val recordProbe = createTestProbe[TemperatureRecorded]()
      deviceActor1 ! RecordTemperature(requestId = 0, 1.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(requestId = 0))
      deviceActor2 ! RecordTemperature(requestId = 1, 2.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(requestId = 1))
    }

    "当传入不存在的groupId时请求被忽略" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 向DeviceGroup Actor中注册一个组的设备Actor
      groupActor ! RequestTrackDevice("wrongGroup", "device1", probe.ref)
      // 真实开发环境应该返回错误提示，不能不返回任何结果，将会导致调用端等待直到超时
      probe.expectNoMessage(400.milliseconds)
    }

    "当重复注册同一个设备时返回同一个设备信息" in {
      val probe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 向设备组注册一个设备，命名为device1
      groupActor ! RequestTrackDevice("group", "device1", probe.ref)
      val registered1 = probe.receiveMessage()
      // 向设备组再次注册一个设备，重复命名为device1
      groupActor ! RequestTrackDevice("group", "device1", probe.ref)
      val registered2 = probe.receiveMessage()
      // 判断返回的两个设备引用是否为同一个
      registered1.device should ===(registered2.device)
    }

    "指定组Id查询设备Id集合" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 向设备组注册两个设备
      groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
      registeredProbe.receiveMessage()
      groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
      registeredProbe.receiveMessage()
      // 指定组Id，看返回的设备Id集合是否是刚刚注册的设备Id集合
      val deviceListProbe = createTestProbe[ReplyDeviceList]()
      groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
      deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2")))
    }

    "请求添加3个设备，下线设备1，然后列出剩余的设备" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 向指定组添加一个设备，id为device1
      groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
      val registered1 = registeredProbe.receiveMessage()
      val toShutDown = registered1.device
      // 向指定组添加一个设备，id为device2
      groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
      registeredProbe.receiveMessage()
      // 向指定组添加一个设备，id为device3
      groupActor ! RequestTrackDevice("group", "device3", registeredProbe.ref)
      registeredProbe.receiveMessage()
      val deviceListProbe = createTestProbe[ReplyDeviceList]()
      // 列出所有设备Id
      groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
      deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2", "device3")))
      // 请求停止设备1
      toShutDown ! Passivate
      registeredProbe.expectTerminated(toShutDown, registeredProbe.remainingOrDefault)
      // 使用awaitAssert方式，因为对于下线的设备，设备组可能需要一段时间来发现它已经下线
      registeredProbe.awaitAssert {
        groupActor ! RequestDeviceList(requestId = 1, groupId = "group", deviceListProbe.ref)
        deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device2", "device3")))
      }
    }

    "获取设备组中所有设备的温度信息" in {
      val registeredProbe = createTestProbe[DeviceRegistered]()
      val groupActor = spawn(DeviceGroup("group"))
      // 添加三个设备
      groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
      val deviceActor1 = registeredProbe.receiveMessage().device
      groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
      val deviceActor2 = registeredProbe.receiveMessage().device
      groupActor ! RequestTrackDevice("group", "device3", registeredProbe.ref)
      registeredProbe.receiveMessage()
      // 向前两个设备设置温度值，第三个不设置，也就是没有温度信息
      val recordProbe = createTestProbe[TemperatureRecorded]()
      deviceActor1 ! RecordTemperature(requestId = 0, 1.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(requestId = 0))
      deviceActor2 ! RecordTemperature(requestId = 1, 2.0, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(requestId = 1))
      // 验证返回的温度信息是否符合预期
      val allTempProbe = createTestProbe[RespondAllTemperatures]()
      groupActor ! RequestAllTemperatures(requestId = 0, groupId = "group", allTempProbe.ref)
      allTempProbe.expectMessage(
        RespondAllTemperatures(
          requestId = 0,
          temperatures = Map("device1" -> Temperature(1.0), "device2" -> Temperature(2.0), "device3" -> TemperatureNotAvailable)
        )
      )
    }

  }

}
