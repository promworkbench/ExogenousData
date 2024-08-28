package org.processmining.qut.exogenousdata.ab.jobs.bases;

import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;

public interface RMMTesting<T,J> {
	
	public abstract List<Configuration<T,J,Long,Double>> getConfigurations();
	public abstract FileInputStream getDumpFile();
	
	public default void run() {
		for(Configuration<T,J,Long,Double> config : getConfigurations()) {
			StopWatch watch = new StopWatch();
			TestingResult<Long,Double> tester = config.makeTesting();
			watch.start();
			J res = _run(config);
			watch.split();
			tester.addTiming("disc", watch.getSplitTime());
			config.setResult(res);
			double measure = _measure();
			watch.split();
			tester.addTiming("disc", watch.getSplitTime());
		}
	}
	
	public default void recordMemoryUsage(Runnable runnable, int runTimeSecs) {
	    try {
	    	
	        CompletableFuture<Void> futureRun = CompletableFuture.runAsync(runnable);
	        futureRun.
	        long mem = 0;
	        while( !futureRun.isDone()) {
		            for (int cnt = 0; cnt < runTimeSecs; cnt++) {
		                long memUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		                mem = memUsed > mem ? memUsed : mem;
		                try {
		                    TimeUnit.SECONDS.sleep(1);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                }
		            }
		            ;
		            System.out.println("Max memory used (gb): " + mem/1000000000D);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public abstract J _run(Configuration<T,J,Long,Double> config);
	
	public default void measure() {
		
	}
	public abstract double _measure();
	
	public default void writeOutError() {
		
	}

}
