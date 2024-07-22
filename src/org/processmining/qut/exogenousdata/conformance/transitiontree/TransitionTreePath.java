package org.processmining.qut.exogenousdata.conformance.transitiontree;

public interface TransitionTreePath<T extends TransitionTreeFlow> {
	
	abstract void append(T flow);
	
	abstract Iterable<String> variant();

}
