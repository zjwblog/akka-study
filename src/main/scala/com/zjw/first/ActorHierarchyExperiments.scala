package com.zjw.first

import akka.actor.typed.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

object ActorHierarchyExperiments extends App {
  val customConf = ConfigFactory.parseString(
    """
      |akka.log-config-on-start = on
      |
      |""".stripMargin)
  private val config: Config = ConfigFactory.load(customConf)
  val system = ActorSystem(Main(), "AkkaTestSystem", config)
  system ! "start"
  system.terminate()
}
