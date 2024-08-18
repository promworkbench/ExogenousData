package org.processmining.qut.exogenousdata.ds.timeseries.norm;

import java.util.stream.Collectors;

import org.processmining.qut.exogenousdata.ds.timeseries.data.RealTimePoint;
import org.processmining.qut.exogenousdata.ds.timeseries.data.RealTimeSeries;

public class SimpleTimeSeriesNoramliser 
	implements TimeSeriesNormaliser<RealTimeSeries> {

	public RealTimeSeries normalise(RealTimeSeries series) {
		double mean = series.computeMean();
		double std = series.computeStandardDeviation();
		return new RealTimeSeries(
				series.getName(),
				series.getColor(),
				series.getPoints().stream()
				.map(p -> new RealTimePoint(
						p.getTime(), 
						(p.getValue() - mean) / std) 
				)
				.collect(Collectors.toList())
		);
	}

}
