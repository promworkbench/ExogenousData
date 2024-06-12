package org.processmining.qut.exogenousdata.steps.transform;

import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousdata.steps.transform.data.TransformedAttribute;

/**
 * A common set of functions for all transformation functions.
 * 
 * @author n7176546
 *
 */
public interface Transformer {

	/**
	 * Given a SubSeries of exogenous data, performs the transformation and returns the attribute to annotate.
	 * @param subtimeseries The sub-timeseries to transform 
	 * @return The transformed attribute to annotate with.
	 */
	public TransformedAttribute transform(SubSeries subtimeseries);
	
}
