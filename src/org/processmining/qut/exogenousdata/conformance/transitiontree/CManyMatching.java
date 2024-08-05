package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class CManyMatching implements Matching<XEvent, TTFlowWithGuard> {
	
	private PNWDTransitionTree tree;
	private Map<String, List<MatchingStep>> history;
	
	public CManyMatching(PNWDTransitionTree tree) {
		super();
		this.tree = tree;
		this.history = new HashMap<>();
	}

	public Iterable<MatchingStep<XEvent, TTFlowWithGuard>> 
		getPath(XTrace trace) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterable<Iterable<MatchingStep<XEvent, TTFlowWithGuard>>> 
		getAllPaths(XTrace trace) {
		// TODO Auto-generated method stub
		return null;
	}

}
