package org.processmining.qut.exogenousdata.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.processmining.log.utils.XUtils;

public class EventyUtils {
	
	private EventyUtils() {};
	
	/**
	 * Returns the concept:name of the given event.
	 * @param event
	 * @return null or concept:name as a String.
	 */
	public static String getConcept(XEvent event) {
		return XUtils.getConceptName(event);
	}
	
	/**
	 * Returns the date that can be extracted from the event.
	 * Requires that the event has a "time:timestamp" Attribute.
	 * @param event
	 * @return null or Date for time:timestamp
	 */
	public static Date getTimestamp(XEvent event) {
		return XUtils.getTimestamp(event);
	}
	
	public static Map<String,Object> extractDataState(XEvent event){
		Map<String,Object> ret = new HashMap<>();
		for( Entry<String, XAttribute> attr : event.getAttributes().entrySet()) {
			ret.put(attr.getKey(),
					XUtils.getAttributeValue(
							attr.getValue())
			);
		}
		return ret;
	}
	
	public static Map<String,Object> updateAndExtractDataState(
			Map<String,Object> state,
			XEvent event
			){
		Map<String,Object> ret = new HashMap<>();
		ret.putAll(state);
		ret.putAll(extractDataState(event));
		return ret;
		
	}

}
