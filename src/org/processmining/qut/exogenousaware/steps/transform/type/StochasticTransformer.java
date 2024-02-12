package org.processmining.qut.exogenousaware.steps.transform.type;

import org.processmining.qut.exogenousaware.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousaware.steps.transform.data.StochasticDummyAttribute;
import org.processmining.qut.exogenousaware.steps.transform.data.TransformedAttribute;

/*
 * A simple transform that does nothing but makes an attribute to trace back the subseries.
 */
public class StochasticTransformer implements Transformer  {

	public TransformedAttribute transform(SubSeries subtimeseries) {
		return new StochasticDummyAttribute("dummy", 1, null, "slpned", subtimeseries);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "slpned:lookback";
	}

}
