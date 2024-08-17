package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import org.deckfour.xes.model.XLog;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TransitionTree;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TransitionTreeFlow;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TransitionTreeNode;

public interface BookkeepingEngine<
	T extends TransitionTreeNode, V extends TransitionTreeFlow> {
	
	public abstract double computeBookkeeping(
			TransitionTree<T,V> tree, Matching<String, V> matcher, XLog log);
	
	public abstract double computeBookkeeping(
			V flow, Matching<String, V> matcher, XLog log);

}
