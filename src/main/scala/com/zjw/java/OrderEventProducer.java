package com.zjw.java;

import com.lmax.disruptor.RingBuffer;

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/28
 */
public class OrderEventProducer {
  private final RingBuffer<OrderEvent> ringBuffer;
  public OrderEventProducer(RingBuffer<OrderEvent> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }
  public void onData(String orderId) {
    long sequence = ringBuffer.next();
    try {
      OrderEvent orderEvent = ringBuffer.get(sequence);
      orderEvent.setId(orderId);
    } finally {
      ringBuffer.publish(sequence);
    }
  }
}
