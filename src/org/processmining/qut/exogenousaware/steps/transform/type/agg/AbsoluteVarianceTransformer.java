package org.processmining.qut.exogenousaware.steps.transform.type.agg;

import org.processmining.qut.exogenousaware.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousaware.steps.transform.data.TransformedAttribute;
import org.processmining.qut.exogenousaware.steps.transform.type.Transformer;

import lombok.Builder;
import lombok.Builder.Default;

/*
 * A transformer that converts the values of the series into variance from the given mean 
 * in terms of the ratio of standard deviations.
 */

@Builder
public class AbsoluteVarianceTransformer implements Transformer {
	
	@Default private double mean = 0.0;
	@Default private double std = 1.0;
	@Default private String name = "abs:variance";

	public TransformedAttribute transform(SubSeries subtimeseries) {
		double var = 
				subtimeseries.getYSeries()
				.stream()
				.map( y -> (Math.abs(y-mean)/std))
				.reduce(Double::sum).get();
//		System.out.println("computed variance :: " + var);
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+":abs:variance", var);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

}
