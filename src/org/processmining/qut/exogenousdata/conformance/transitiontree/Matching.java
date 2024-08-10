package org.processmining.qut.exogenousdata.conformance.transitiontree;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.utils.EventyUtils;

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
	
	
	public default int computeCost(
			Iterable<MatchingStep<XEvent, TTFlowWithGuard>> path,
			XTrace trace) {
		int len = trace.size();
		int noskip = 0;
		int acts = 0;
		boolean term = false;
		for(MatchingStep<XEvent, TTFlowWithGuard> step : path) {
			if (!step.isSkip()) {
				noskip += 1;
			}
			if (step.getModel().isPresent()) {
				term = step.getModel().get().tgt().isTerminal();
				String mact = step.getModel().get().label();
				if (step.getLog().isPresent()) {
					String lact = EventyUtils.getConcept(step.getLog().get());
					if (!lact.equals(mact)) {
						acts += 1;
					}
				}
			} else {
				term = false;
			}
		}
		return len - noskip + acts + (!term ? 1 : 0);
	}

}
