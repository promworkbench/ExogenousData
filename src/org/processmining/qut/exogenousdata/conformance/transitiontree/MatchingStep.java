package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Optional;

public interface MatchingStep<T,V>{
	
	abstract public Optional<T> getLeft();
	
	abstract public Optional<V> getRight();
	
	abstract public boolean isSkip();

}
