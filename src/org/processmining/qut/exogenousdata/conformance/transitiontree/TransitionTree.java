package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Set;

import org.deckfour.xes.model.XTrace;

public interface TransitionTree<
	T extends TransitionTreeNode, V extends TransitionTreeFlow> {
	
	
	abstract public Set<V> getFlows();
	
	abstract public Set<T> getNodes();
	
	abstract public T getRoot();
	
	abstract public T hasNodeFor(XTrace trace);

}
