package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Optional;

public interface MatchingStep<T,V>{
	
	public static enum StepType {
		sync,
		logmove,
		modelmove;
	}
	
	/**
	 * Gets the possible event for this step.
	 * @return
	 */
	abstract public Optional<T> getLog();
	
	/**
	 * Gets the possible flow for this step.
	 * @return
	 */
	abstract public Optional<V> getModel();
	
	/**
	 * Returns if the step is a skip (or a logmove).
	 * @return
	 */
	abstract public boolean isSkip();

}
