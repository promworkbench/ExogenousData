package org.processmining.qut.exogenousdata.conformance.transitiontree;

import org.deckfour.xes.model.XTrace;

public interface Matching<T,V> {
	
	/**
	 * Returns all paths that the matching believes are optimal.
	 * @param trace
	 * @return
	 */
	abstract public Iterable<MatchingStep<T,V>> 
		getPath(XTrace trace);
	
	/**
	 * Return a path that the matching believes is optimal.
	 * @param trace
	 * @return
	 */
	abstract public Iterable<Iterable<MatchingStep<T,V>>> 
		getAllPaths(XTrace trace);

}
