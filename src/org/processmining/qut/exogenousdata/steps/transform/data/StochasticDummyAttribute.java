package org.processmining.qut.exogenousdata.steps.transform.data;

import org.deckfour.xes.extension.XExtension;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;

/*
 * A dummy attribute to trace back to the subseries for slpned discovery.
 */
public class StochasticDummyAttribute extends TransformedAttribute {

	public StochasticDummyAttribute(String key, double value) {
		super(key, value);
	}
	
	public StochasticDummyAttribute(String key, double value, XExtension extension, String transform, SubSeries source) {
		super(key, value, extension, transform, source);
	}
	
	

}
