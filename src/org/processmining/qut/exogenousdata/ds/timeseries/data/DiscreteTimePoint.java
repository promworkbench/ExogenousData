package org.processmining.qut.exogenousdata.ds.timeseries.data;

public class DiscreteTimePoint implements TimeSeriesPoint<String, Double> {
	
	private String discreteValue;
	private Double time;
	
	public DiscreteTimePoint(String discreteValue, Double time) {
		this.discreteValue = discreteValue;
		this.time = time;
	}

	public Double getTime() {
		return time;
	}

	public String getValue() {
		return discreteValue;
	}

}
