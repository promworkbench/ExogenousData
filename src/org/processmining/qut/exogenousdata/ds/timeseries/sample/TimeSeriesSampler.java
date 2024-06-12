package org.processmining.qut.exogenousdata.ds.timeseries.sample;

import org.processmining.qut.exogenousdata.ds.timeseries.data.RealTimeSeries;

public interface TimeSeriesSampler< T extends RealTimeSeries> {

	public T sample(T series);
}
