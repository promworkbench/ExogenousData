package org.processmining.qut.exogenousdata.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
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
	
	public static List<String> getLabels(XTrace trace){	
		return trace.stream()
			.map(t -> XUtils.getConceptName(t))
			.collect(Collectors.toList());
	}
	
	public static XTrace createDummyVariant(String ...labels) {
		String variant = Arrays.asList(labels)
				.stream().reduce("", String::concat);
		XTrace ret = new XTraceImpl(new XAttributeMapImpl(
				new HashMap<String,XAttribute>() {{
			put("concept:name", 
				new XAttributeLiteralImpl(
						"concept:name", "dummy_trace_"+variant
				)
			);
		}}));
		
		for(String label : Arrays.asList(labels)) {
			ret.add(new XEventImpl(new XAttributeMapImpl(
				new HashMap<String,XAttribute>() {{
					put("concept:name", 
						new XAttributeLiteralImpl(
								"concept:name", label
						));
				}}
			)));
		}
		
		return ret;
	}

}
