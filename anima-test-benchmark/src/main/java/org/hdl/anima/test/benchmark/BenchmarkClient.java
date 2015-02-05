package org.hdl.anima.test.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class BenchmarkClient extends AbstractBenchmarkClient {

    @Override
    public ClientRunnable getClientRunnable(String targetIP, int targetPort, int serviceId,int requestSize, int rpcTimeout,
                                            CyclicBarrier barrier,
                                            CountDownLatch latch, long startTime,long endTime) {
        return new SimpleProcessorBenchmarkClientRunnable(targetIP, targetPort, serviceId, requestSize,rpcTimeout,
                                                         barrier, latch, startTime, endTime);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkClient().run(args);
    }
}
