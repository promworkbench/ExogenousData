package org.processmining.qut.exogenousdata.conformance.transitiontree;

public interface TransitionTreeNode {
	
	abstract public TransitionTreePath<? extends TransitionTreeFlow> getPath();
	
	abstract public Iterable<String> getVariant();
	
	abstract public <T extends TransitionTreeFlow> T getSuccFlow();
	
	abstract public Iterable<? extends TransitionTreeFlow> getOutgoingFlows();
	
	abstract public boolean isRoot();
	
	abstract public boolean isTerminal();
}
