package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Optional;

public interface MatchingStep<T,V>{
	
	abstract public Optional<T> getLog();
	
	abstract public Optional<V> getModel();
	
	abstract public boolean isSkip();

}
