package org.processmining.qut.exogenousdata.steps.transform.type.agg;

import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousdata.steps.transform.data.TransformedAttribute;
import org.processmining.qut.exogenousdata.steps.transform.type.Transformer;

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
		double var;
		if (std > 0) {
			var = 
					subtimeseries.getYSeries()
					.stream()
					.map( y -> (Math.abs(y-mean)/std))
					.reduce(Double::sum).get();
//			System.out.println("computed variance :: " + var +" using "+std);
		} else {
			var = 0;
		}
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+":abs:variance", var);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

}
