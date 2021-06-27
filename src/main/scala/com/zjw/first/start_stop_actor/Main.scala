package com.zjw.first.start_stop_actor

import akka.actor.typed.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem(StartStopActor1(), "StartStopActor")
    system ! "stop"
  }
}
