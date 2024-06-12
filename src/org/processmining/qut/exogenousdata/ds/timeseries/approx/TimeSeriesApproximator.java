package org.processmining.qut.exogenousdata.ds.timeseries.approx;

import org.processmining.qut.exogenousdata.ds.timeseries.data.TimeSeries;

public interface TimeSeriesApproximator<I extends TimeSeries, O extends TimeSeries> {
	
	public O approximate(I series);

}
