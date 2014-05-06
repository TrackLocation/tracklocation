package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.Intent;

import com.dagrest.tracklocation.log.LogManager;

public class ScheduledActionExecutor {

	private static final int THREAD_POOL_MAX = 5;
	
	private ScheduledExecutorService scheduledExecutorService;
	//private ScheduledFuture<String> scheduledFuture;
	private Runnable beeper;
	private ScheduledFuture beeperHandle;
	private Future longRunningTaskFurure;

	public Runnable getBeeper() {
		return beeper;
	}

	public ScheduledActionExecutor(int seconds) {
		super();
		scheduledExecutorService = Executors.newScheduledThreadPool(THREAD_POOL_MAX);

	     beeper = new Runnable() {
		       public void run() { 
		    		LogManager.LogInfoMsg(this.getClass().getName(), "ScheduledActionExecutor", 
		        		new Date().toString());
		       };
		     };
		     
	     beeperHandle =
	    	 scheduledExecutorService.scheduleAtFixedRate(beeper, 0, seconds, TimeUnit.SECONDS);

	     longRunningTaskFurure = scheduledExecutorService.submit(beeper); 
	     
//		scheduledFuture =
//		    scheduledExecutorService.scheduleAtFixedRate(new Callable<String>() {
//		        public String call() throws Exception {
//		            //System.out.println("Executed!");
//		            return "Called!";
//		        }
//		    },
//		    0
//		    seconds,
//		    TimeUnit.SECONDS);
	}

 	   //System.out.println("result = " + scheduledFuture.get());
	
	public void shutdown() {
//	    beeperHandle.cancel(true);
		longRunningTaskFurure.cancel(true);
		scheduledExecutorService.shutdown();
	}

}
