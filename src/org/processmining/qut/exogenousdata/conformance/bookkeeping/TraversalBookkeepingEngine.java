package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTNode;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TransitionTree;

public class TraversalBookkeepingEngine implements 
	BookkeepingEngine<TTNode, TTFlowWithGuard> {

	public double computeBookkeeping(TransitionTree<TTNode, TTFlowWithGuard> tree,
			Matching<XEvent, TTFlowWithGuard> matcher, XLog log) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double computeBookkeeping(TTFlowWithGuard flow, Matching<XEvent, TTFlowWithGuard> matcher, XLog log) {
		// TODO Auto-generated method stub
		return 0;
	}



}
