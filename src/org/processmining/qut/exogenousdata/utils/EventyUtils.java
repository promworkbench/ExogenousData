package org.processmining.qut.exogenousdata.utils;

import org.deckfour.xes.model.XEvent;
import org.xeslite.common.XUtils;

public class EventyUtils {
	
	private EventyUtils() {};
	
	/**
	 * Returns the concept:name of the given event.
	 * @param event
	 * @return concept:name as a String.
	 */
	public static String getConcept(XEvent event) {
		return XUtils.getConceptName(event);
	}

}
