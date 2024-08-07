package org.processmining.qut.exogenousdata.utils;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class LoggyUtils {
	
	private LoggyUtils () {};
	
	public static int findLongestTrace(XLog log) {
		int longest = 0;
		
		for(XTrace trace: log) {
			if (trace.size() > longest) {
				longest = trace.size();
			}
		}
		
		return longest;
	}
	
	public static Set<String> getAllEventConcepts(XLog log){
		Set<String> ret = new HashSet<>();
		for(XTrace trace : log) {
			for(XEvent ev: trace) {
				ret.add(EventyUtils.getConcept(ev));
			}
		}
		return ret;
	}
}
