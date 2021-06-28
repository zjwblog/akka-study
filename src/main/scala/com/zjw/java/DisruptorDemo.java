package com.zjw.java;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by zjwblog<co.zjwblog@gmail.com> on 2021/6/28
 */
@Slf4j
public class DisruptorDemo {

  public static void main(String[] args) throws InterruptedException {
    Disruptor<OrderEvent> disruptor = new Disruptor<>(
        OrderEvent::new,
        1024 * 1024,
        Executors.defaultThreadFactory(),
        // 这里的枚举修改为多生产者
        ProducerType.MULTI,
        new YieldingWaitStrategy()
    );
    disruptor.handleEventsWithWorkerPool(new OrderEventHandler(), new OrderEventHandler());
    disruptor.start();
    RingBuffer<OrderEvent> ringBuffer = disruptor.getRingBuffer();
    OrderEventProducer eventProducer = new OrderEventProducer(ringBuffer);
    // 创建一个线程池，模拟多个生产者
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(100);
    for (int i = 0; i < 100; i++) {
      fixedThreadPool.execute(() -> eventProducer.onData(UUID.randomUUID().toString()));
    }
  }
}

