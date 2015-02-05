package org.hdl.anima.common.threadpool.cached;

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
 * 此线程池可伸缩，线程空闲一分钟后回收，新请求重新创建线程，来源于：<code>Executors.newCachedThreadPool()</code>
 * 
 * @see java.util.concurrent.Executors#newCachedThreadPool()
 * @author william.liangf
 */
public class CachedThreadPool implements ThreadPool {

    public Executor getExecutor(AppConf conf) {
        String name = conf.get(Constants.THREAD_NAME_KEY, Constants.DEFAULT_THREAD_NAME);
        int cores = conf.getInt(Constants.CORE_THREADS_KEY, Constants.DEFAULT_CORE_THREADS);
        int threads = conf.getInt(Constants.THREADS_KEY, Integer.MAX_VALUE);
        int queues = conf.getInt(Constants.QUEUES_KEY, Constants.DEFAULT_QUEUES);
        int alive = conf.getInt(Constants.ALIVE_KEY, Constants.DEFAULT_ALIVE);
        return new ThreadPoolExecutor(cores, threads, alive, TimeUnit.MILLISECONDS, 
        		queues == 0 ? new SynchronousQueue<Runnable>() : 
        			(queues < 0 ? new LinkedBlockingQueue<Runnable>() 
        					: new LinkedBlockingQueue<Runnable>(queues)),
        		new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
    }
}