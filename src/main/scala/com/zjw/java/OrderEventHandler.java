package com.zjw.java;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/28
 */
@Slf4j
public class OrderEventHandler implements EventHandler<OrderEvent>, WorkHandler<OrderEvent> {
  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) {
    log.info("event: {}, sequence: {}, endOfBatch: {}", event, sequence, endOfBatch);
  }
  @Override
  public void onEvent(OrderEvent event) {
    log.info("event: {}", event);
  }
}

