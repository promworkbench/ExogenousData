package org.processmining.qut.exogenousdata.ds.timeseries.reduce;

import org.processmining.qut.exogenousdata.ds.timeseries.data.TimeSeries;

public interface TimeSeriesReducer<T extends TimeSeries> {

	public T reduce(T series);
	
}
