package org.hdl.anima.common.threadpool.fixed;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hdl.anima.AppConf;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.common.threadpool.ThreadPool;
import org.hdl.anima.common.threadpool.support.AbortPolicyWithReport;
import org.hdl.anima.remoting.Constants;

/**
 * 此线程池启动时即创建固定大小的线程数，不做任何伸缩，来源于：<code>Executors.newFixedThreadPool()</code>
 * 
 * @see java.util.concurrent.Executors#newFixedThreadPool(int)
 * @author qiuhd
 */
public class FixedThreadPool implements ThreadPool {

    public Executor getExecutor(AppConf conf) {
        String name = conf.get(Constants.THREAD_NAME_KEY, Constants.DEFAULT_THREAD_NAME);
        int threads = conf.getInt(Constants.THREADS_KEY, Constants.DEFAULT_THREADS);
        int queues = conf.getInt(Constants.QUEUES_KEY, Constants.DEFAULT_QUEUES);
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, 
        		queues == 0 ? new SynchronousQueue<Runnable>() : 
        			(queues < 0 ? new LinkedBlockingQueue<Runnable>() 
        					: new LinkedBlockingQueue<Runnable>(queues)),
        		new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
    }

}