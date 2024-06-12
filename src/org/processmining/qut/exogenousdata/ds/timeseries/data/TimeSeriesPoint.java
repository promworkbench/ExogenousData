package org.processmining.qut.exogenousdata.ds.timeseries.data;

public interface TimeSeriesPoint<V, T> {

	public T getTime();
	
	public V getValue();
	
}
