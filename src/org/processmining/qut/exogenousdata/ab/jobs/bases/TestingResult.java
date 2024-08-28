package org.processmining.qut.exogenousdata.ab.jobs.bases;

import java.util.Map;

public interface TestingResult<T,J> {
	
	public abstract Map<String,T> getTimings();
	public abstract Map<String,J> getMeasurements();
	
	public abstract void addTiming(String name, T ms);
	public abstract void addMeasurement(String name, J measure);

}
