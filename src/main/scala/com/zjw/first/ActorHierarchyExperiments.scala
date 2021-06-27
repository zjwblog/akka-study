package com.zjw.first

import akka.actor.typed.ActorSystem

object ActorHierarchyExperiments extends App {
  val system = ActorSystem(Main(), "AkkaTestSystem")
  system ! "start"
}
