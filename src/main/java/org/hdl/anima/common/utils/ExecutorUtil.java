package org.hdl.anima.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hdl.anima.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author qiuhd
 * @since  2014年9月3日
 */
public class ExecutorUtil {

	 private static final Logger logger = LoggerFactory.getLogger(ExecutorUtil.class);
	    private static final ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(0, 1,
	            0L, TimeUnit.MILLISECONDS,
	            new LinkedBlockingQueue<Runnable>(100),
	            new NamedThreadFactory("Close-ExecutorService-Timer", true)); 

	    public static boolean isShutdown(Executor executor) {
	        if (executor instanceof ExecutorService) {
	            if (((ExecutorService) executor).isShutdown()) {
	                return true;
	            }
	        }
	        return false;
	    }
	    public static void gracefulShutdown(Executor executor, int timeout) {
	        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
	            return;
	        }
	        final ExecutorService es = (ExecutorService) executor;
	        try {
	            es.shutdown(); // Disable new tasks from being submitted
	        } catch (SecurityException ex2) {
	            return ;
	        } catch (NullPointerException ex2) {
	            return ;
	        }
	        try {
	            if(! es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
	                es.shutdownNow();
	            }
	        } catch (InterruptedException ex) {
	            es.shutdownNow();
	            Thread.currentThread().interrupt();
	        }
	        if (!isShutdown(es)){
	            newThreadToCloseExecutor(es);
	        }
	    }
	    public static void shutdownNow(Executor executor, final int timeout) {
	        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
	            return;
	        }
	        final ExecutorService es = (ExecutorService) executor;
	        try {
	            es.shutdownNow();
	        } catch (SecurityException ex2) {
	            return ;
	        } catch (NullPointerException ex2) {
	            return ;
	        }
	        try {
	            es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
	        } catch (InterruptedException ex) {
	            Thread.currentThread().interrupt();
	        }
	        if (!isShutdown(es)){
	            newThreadToCloseExecutor(es);
	        }
	    }

	    private static void newThreadToCloseExecutor(final ExecutorService es) {
	        if (!isShutdown(es)) {
	            shutdownExecutor.execute(new Runnable() {
	                public void run() {
	                    try {
	                        for (int i=0;i<1000;i++){
	                            es.shutdownNow();
	                            if (es.awaitTermination(10, TimeUnit.MILLISECONDS)){
	                                break;
	                            }
	                        }
	                    } catch (InterruptedException ex) {
	                        Thread.currentThread().interrupt();
	                    } catch (Throwable e) {
	                        logger.warn(e.getMessage(), e);
	                    } 
	                }
	            });
	        }
	    }
}

