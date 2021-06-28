package com.zjw.iot

import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.slf4j
import org.slf4j.LoggerFactory

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/28
 */
// 采用继承scala log4j api方式使用log4j2
object Main extends Logging {
  // 采用log4j自带的api使用log4j2
  private[this] val log: Logger = LogManager.getLogger(classOf[Main])
  // 采用slf4j api使用log4j2
  private[this] val log2: slf4j.Logger = LoggerFactory.getLogger(classOf[Main])
  def main(args: Array[String]): Unit = {
    logger.info("hello")
    log.info("Hello")
    log2.info("HELLO")
  }
}

class Main
