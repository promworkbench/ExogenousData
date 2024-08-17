package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.List;

import org.deckfour.xes.model.XTrace;

public interface Matching<T,V> {
	
	/**
	 * Returns all paths that the matching believes are optimal.
	 * @param trace
	 * @return
	 */
	abstract public List<MatchingStep<T,V>> 
		getPath(XTrace trace);
	
	/**
	 * Return a path that the matching believes is optimal.
	 * @param trace
	 * @return
	 */
	abstract public Iterable<List<MatchingStep<T,V>>> 
		getAllPaths(XTrace trace);
	
	
	public default int computeCost(
			Iterable<MatchingStep<String, TTFlowWithGuard>> path,
			XTrace trace) {
		int len = trace.size();
		int pathLen = 0;
		int noskip = 0;
		int acts = 0;
		boolean term = false;
		for(MatchingStep<String, TTFlowWithGuard> step : path) {
			pathLen++;
			if (step.isSkip()) {
				noskip++;
			}
			if (step.getModel().isPresent()) {
				term = step.getModel().get().tgt().isTerminal();
				String mact = step.getModel().get().label();
				if (step.getLog().isPresent()) {
					String lact = step.getLog().get();
					if (!lact.equals(mact)) {
						acts++;
					}
				}
			} else {
				term = false;
			}
		}
		return (len - (pathLen - noskip)) + acts + (!term ? 1 : 0);
	}

}
