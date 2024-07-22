package org.processmining.qut.exogenousdata.conformance.transitiontree;

public interface TransitionTreeFlow {

	abstract public <T extends TransitionTreeNode> T src();
	
	abstract public <T extends TransitionTreeNode> T tgt();
	
	abstract public String label();
}
