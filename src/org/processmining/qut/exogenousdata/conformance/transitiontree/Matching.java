package org.processmining.qut.exogenousdata.conformance.transitiontree;

import org.deckfour.xes.model.XTrace;

public interface Matching<T,V> {
	
	public static String SKIPPER = ">>";
	
	abstract public Iterable<MatchingStep<T,V>> 
		getPath(XTrace trace);
	
	abstract public Iterable<Iterable<MatchingStep<T,V>>> 
		getAllPaths(XTrace trace);

}
