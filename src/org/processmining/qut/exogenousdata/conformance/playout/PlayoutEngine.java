package org.processmining.qut.exogenousdata.conformance.playout;

public interface PlayoutEngine<T, V extends PlayoutTrace> {
	
	public abstract Iterable<V> generateTraces(T model, int maxLength);

}
