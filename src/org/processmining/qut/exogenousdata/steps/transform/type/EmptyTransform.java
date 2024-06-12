package org.processmining.qut.exogenousdata.steps.transform.type;

import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousdata.steps.transform.data.TransformedAttribute;

public class EmptyTransform implements Transformer {
	
	public EmptyTransform() {
		
	}

	public TransformedAttribute transform(SubSeries subtimeseries) {
		return null;
	}

	public String getName() {
		return "??";
	}

}
