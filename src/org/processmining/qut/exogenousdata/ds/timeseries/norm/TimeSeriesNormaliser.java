package org.processmining.qut.exogenousdata.ds.timeseries.norm;

import org.processmining.qut.exogenousdata.ds.timeseries.data.TimeSeries;

public interface TimeSeriesNormaliser<T extends TimeSeries> {

	public T normalise(T series);
	
}
