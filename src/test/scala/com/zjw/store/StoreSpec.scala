package com.zjw.store

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.apache.logging.log4j.scala.Logging
import org.scalatest.wordspec.AnyWordSpecLike

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
class StoreSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike with Logging {
  "Store Actor" must {
    "保存数据" in {
      val probe = createTestProbe[Store.ResponseSetElement]()
      val storeActor = spawn(Store())
      storeActor ! Store.SetElement("1", "Hello", "World", probe.ref)
      val response = probe.receiveMessage()
      response.result should ===("success")
      response.requestId should ===("1")
    }
    "保存获取数据" in {
      val probe1 = createTestProbe[Store.ResponseSetElement]()
      val probe2 = createTestProbe[Store.ResponseGetElement]()
      val storeActor = spawn(Store())
      storeActor ! Store.SetElement("1", "Hello", "World", probe1.ref)
      val response1 = probe1.receiveMessage()
      response1.result should ===("success")
      response1.requestId should ===("1")
      storeActor ! Store.GetElement("2", "Hello", probe2.ref)
      val response2: Store.ResponseGetElement = probe2.receiveMessage()
      response2.result should ===(Some("World"))
      storeActor ! Store.GetElement("2", "None", probe2.ref)
      val response3: Store.ResponseGetElement = probe2.receiveMessage()
      response3.result should ===(None)
    }
    "保存获取所有数据" in {
      val probe1 = createTestProbe[Store.ResponseSetElement]()
      val probe2 = createTestProbe[Store.ResponseGetAllElement]()
      val storeActor = spawn(Store(), "store")
      logger.info(storeActor.path)
      storeActor ! Store.SetElement("1", "Hello1", "World1", probe1.ref)
      storeActor ! Store.SetElement("2", "Hello2", "World2", probe1.ref)
      storeActor ! Store.SetElement("3", "Hello3", "World3", probe1.ref)
      storeActor ! Store.SetElement("4", "Hello4", "World4", probe1.ref)

      storeActor ! Store.GetAllElement("5", probe2.ref)

      probe2.expectMessage(
        Store.ResponseGetAllElement(
          requestId = "5",
          result = Map("Hello1" -> "World1", "Hello2" -> "World2", "Hello3" -> "World3", "Hello4" -> "World4")
        )
      )
    }
  }
}
