package org.processmining.qut.exogenousdata.ds.timeseries.stats;

public interface Measure {
	
	/**
	 * This calcuates the given measure.
	 * @return
	 */
	public abstract double calculate() throws Throwable;

}
