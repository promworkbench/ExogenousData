package org.processmining.qut.exogenousdata.conformance.transitiontree;

import org.deckfour.xes.model.XTrace;

public interface TransitionTree<
	T extends TransitionTreeNode, V extends TransitionTreeFlow> {
	
	
	abstract public V getFlows();
	
	abstract public T getNodes();
	
	abstract public T getRoot();
	
	abstract public T hasNodeFor(XTrace trace);

}
