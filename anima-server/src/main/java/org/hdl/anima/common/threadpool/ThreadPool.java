package org.hdl.anima.common.threadpool;

import java.util.concurrent.Executor;

import org.hdl.anima.AppConf;

/**
 * ThreadPool
 * @author qiuhd
 *
 */
public interface ThreadPool {
    
    /**
     * 线程池
     * 
     * @param conf 线程参数
     * @return 线程池
     */
    Executor getExecutor(AppConf conf);
}