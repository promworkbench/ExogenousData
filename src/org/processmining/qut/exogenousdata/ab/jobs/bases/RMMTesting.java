package org.processmining.qut.exogenousdata.ab.jobs.bases;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.time.StopWatch;

public interface RMMTesting<T,J> {
	
	public abstract List<Configuration<T,J,Long,Double>> getConfigurations();
	public abstract FileInputStream getDumpFile();
	
	public default void run() {
		int configN = 1;
		for(Configuration<T,J,Long,Double> config : getConfigurations()) {
			System.out.println("[RMMTesting] " + config.toString() +" :: jobnumber=" + configN);
			StopWatch watch = new StopWatch();
			TestingResult<Long,Double> tester = config.makeTesting();
			watch.start();
			J res = recordMemoryUsageOnRun(config);
			watch.split();
			config.setResult(res);
			tester.addTiming("disc", watch.getSplitTime());
			if (res == null) {
				System.out.println("[RMMTesting] [Outcomes] "+ config.toString()+ " [Failed]");
				continue;
			}
			config.setResult(res);
			measure(config, tester);
			watch.split();
			tester.addTiming("conf", watch.getSplitTime());
			configN += 1;
			System.out.println("[RMMTesting] [Outcomes] "+ config.toString() 
				+" "+tester.toString());
		}
	}
	
	public default J recordMemoryUsageOnRun(
			Configuration<T,J,Long,Double> config) {
		Runtime.getRuntime().gc();
	    try {
	        CompletableFuture<J> futureRun = CompletableFuture.supplyAsync(() -> {
	            // some computation
	            return _run(config);
	        });
	        long mem = 0;
	        long startMem = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
	        StopWatch watch = new StopWatch();
	        watch.start();
	        while( !futureRun.isDone()) {
	        	long time = watch.getTime();
	        	if (time > 2500) {
	        		long memUsed = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
		            mem = memUsed > mem ? memUsed : mem;
		            System.out.println("[RMMTesting] Max memory used (gb): " 
		            		+ ((mem - startMem)/(1024 * 1024))
		            		+ " MB."
		            );
		            watch.reset();
		            watch.start();
	        	}
		            
	        }
	        long memUsed = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
		    mem = memUsed > mem ? memUsed : mem;
		    config.getTestingResult().addMeasurement("disc-mem", (double) (mem/(1024.0 * 1024.0)));
	        return futureRun.get();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public abstract J _run(Configuration<T,J,Long,Double> config);
	
	public default void measure(
			Configuration<T,J,Long,Double> config,
			TestingResult<Long,Double> tester) {
		double res = recordMemoryUsageOnMeasure(config);
		tester.addMeasurement("edEMSu", res);
	}
	public abstract double _measure(Configuration<T,J,Long,Double> config);
	
	public default double recordMemoryUsageOnMeasure(
			Configuration<T,J,Long,Double> config) {
	    try {
	    	
	        CompletableFuture<Double> futureRun = CompletableFuture.supplyAsync(() -> {
	            // some computation
	            return _measure(config);
	        });
	        long mem = 0;
	        long startMem = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
	        StopWatch watch = new StopWatch();
	        watch.start();
	        while( !futureRun.isDone()) {
	        	long time = watch.getTime();
	        	if (time > 2500) {
	        		long memUsed = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
		            mem = memUsed > mem ? memUsed : mem;
		            System.out.println("[RMMTesting] Max memory used (gb): " 
		            		+ ((mem - startMem)/(1024 * 1024)));
		            watch.reset();
		            watch.start();
	        	}
		            
	        }
	        long memUsed = Runtime.getRuntime().totalMemory() 
	        		- Runtime.getRuntime().freeMemory();
	        mem = memUsed > mem ? memUsed : mem;
		    config.getTestingResult().addMeasurement("conf-mem", 
		    		(double) (mem/(1024.0 * 1024.0)));
	        return futureRun.get();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return -1;
	}
	
	public default void writeOutError() {
		
	}

}
