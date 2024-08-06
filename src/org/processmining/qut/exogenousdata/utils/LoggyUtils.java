package org.processmining.qut.exogenousdata.utils;

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
}
