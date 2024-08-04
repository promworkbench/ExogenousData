package org.processmining.qut.exogenousdata.utils;

import java.util.Optional;

import org.deckfour.xes.model.XTrace;
import org.xeslite.common.XUtils;

public class TraceyUtils {
	
	private TraceyUtils() {};
	
	public static String getControlFlowVariant(XTrace trace) {
		Optional<String> ret = trace.stream()
			.map(t -> XUtils.getConceptName(t))
			.reduce((c,n) -> c + "," + n);
		if (ret.isPresent()) {
			return "<"+ret.get()+">";
		} else {
			return "<>";
		}
	}

}
