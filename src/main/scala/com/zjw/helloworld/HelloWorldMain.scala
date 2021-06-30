package com.zjw.helloworld

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/29
 */
object HelloWorldMain {
  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup { context =>
      val greeter = context.spawn(HelloWorld(), "greeter")

      Behaviors.receiveMessage ((message: SayHello) => {
        // 3. HelloWorldMain 收到命令，创建了一个 HelloWorldBot Actor，并指定其名字为传入的Akka名字，且发送三次数据
        val replyTo = context.spawn(HelloWorldBot(max = 3), message.name)
        // 4. 向 HelloWorld 发送打招呼
        greeter ! HelloWorld.Greet(message.name, replyTo)
        Behaviors.same
      })
    }
  def main(args: Array[String]): Unit = {
    // 1. 初始化Actor系统，创建一个 HelloWorldMain Actor，
    val system: ActorSystem[HelloWorldMain.SayHello] = ActorSystem(HelloWorldMain(), "HelloWorldMain")
    // 2. 向HelloWorldMain发送Akka命令
    system ! HelloWorldMain.SayHello("Akka")
  }
}
