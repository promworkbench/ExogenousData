package org.processmining.qut.exogenousaware.data;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;

public class ExogenousDatasetAttributes {

	private ExogenousDatasetAttributes() {};
	
//	names of XAttributes related to the exo-series (Trace like sequences).
	public static final String EXOEVENTVALUE = "exogenous:value";
	
	/**
	 * Extracts the recorded exogenous value from an XEvent.
	 * @param xevent an event within an xseries of an exogenous dataset. 
	 */
	public static Object extractExogenousValue(XEvent xevent) {
		XAttributeMap attrs = xevent.getAttributes();
		if (attrs.containsKey(EXOEVENTVALUE)) {
			XAttribute attr = attrs.get(EXOEVENTVALUE);
			if (attr instanceof XAttributeContinuous) {
				return  ((XAttributeContinuous) attr).getValue();
			} else if (attr instanceof XAttributeBoolean) {
				return ((XAttributeBoolean) attr).getValue() ? 1 : 0;
			}
		}
		return 0;
	}
}