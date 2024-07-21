package org.processmining.qut.exogenousdata.conformance.playout;

public interface PlayoutTrace<T extends PlayoutStep> {
	
	public abstract Iterable<T> getSteps(); 

}
