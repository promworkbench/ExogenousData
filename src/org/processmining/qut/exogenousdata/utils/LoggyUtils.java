package org.processmining.qut.exogenousdata.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;

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
	
	public static Set<List<String>> getAllVariants(XLog log){
		Set<List<String>> ret = new HashSet<>();
		for(XTrace trace: log) {
			ret.add(TraceyUtils.getLabels(trace));
		}
		return ret;
	}
	
	public static XLog sampleFromLog(XLog log, int samples) {
		XLog slog = new XLogImpl((XAttributeMap) log.getAttributes().clone());
		Set<Integer> seen = new HashSet();
		Random rand = new Random(29082024);
		for(int i=0; i < samples; i++) {
			int next = rand.nextInt(log.size());
			while(seen.contains(next)) {
				next = rand.nextInt(log.size());
			}
			slog.add(log.get(next));
			seen.add(next);
			if (seen.size() == log.size()) {
				break;
			}
		}
		return slog;
	}
}
