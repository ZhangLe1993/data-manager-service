package com.biubiu.dms.core.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 *   ArrayBlockingQueue：          由数组结构组成的有界阻塞队列。
 　　LinkedBlockingQueue：         由链表结构组成的有界阻塞队列。
 　　PriorityBlockingQueue：       支持优先级排序的无界阻塞队列。
 　　DealyQueue：                  使用优先级队列实现的无界阻塞队列。
 　　SynchronousQueue：            不存储元素的阻塞队列。
 　　LinkedTransferQueue：         由链表结构组成的无界阻塞队列。
 　　LinkedBlockingDeque：         由链表结构组成的双向阻塞队列。
 */
@Configuration
public class ThreadPoolConfig {

    private static final int MAX_POOL_SIZE = 200;
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 1;
    private static final String THREAD_NAME = "websocket-thread-%d";

    /**
     * 队列线程池
     * @return
     */
    @Bean(value = "websocketThreadPool")
    public ExecutorService buildWebsocketThreadPool() {

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(THREAD_NAME).build();

        /**
         * 1. CallerRunsPolicy ：    这个策略重试添加当前的任务，他会自动重复调用 execute() 方法，直到成功。
         2. AbortPolicy ：         对拒绝任务抛弃处理，并且抛出异常。
         3. DiscardPolicy ：       对拒绝任务直接无声抛弃，没有异常信息。
         4. DiscardOldestPolicy ： 对拒绝任务不抛弃，而是抛弃队列里面等待最久的一个线程，然后把拒绝任务加到队列。
         */
        ExecutorService threadPool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                0L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(MAX_POOL_SIZE),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());

        /*ExecutorService pool = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                0L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);*/

        return threadPool;
    }

}
