package org.processmining.qut.exogenousaware.helpers;

import java.util.Random;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;

public class LogHelpers {
	
	@Plugin(
			name = "Sample a log with replacement with the half the number of traces (consistent)",
			parameterLabels = {"Event Log",},
			returnLabels = {"Sampled Log"},
			returnTypes = {XLog.class},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = "QUT",
			author = "A. Banham",
			email = "adam.banham@hdr.qut.edu.au"
	)
	public XLog SampleWithReplacement(UIPluginContext context, XLog givenLog) {
		
		XLog sampleLog = new XLogImpl((XAttributeMap) givenLog.getAttributes().clone());
		Random radomer = new Random( 500 );
		for(int s=0; s < givenLog.size()/2.0; s++) {
			int randomTrace = radomer.nextInt(givenLog.size());
			sampleLog.add((XTrace) givenLog.get(randomTrace).clone());
		}
		
		return sampleLog; 
	}

}
