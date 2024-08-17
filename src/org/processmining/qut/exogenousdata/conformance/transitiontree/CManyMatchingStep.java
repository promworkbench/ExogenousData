package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Objects;
import java.util.Optional;

public class CManyMatchingStep implements MatchingStep<String, TTFlowWithGuard> {
	
	private String event;
	private TTFlowWithGuard flow;
	private StepType type;
	
	public CManyMatchingStep(String event, TTFlowWithGuard flow) {
		super();
		this.event = event;
		this.flow = flow;
		this.type = StepType.sync;
	}
	
	public CManyMatchingStep(String event) {
		super();
		this.event = event;
		this.type = StepType.logmove;
	}

	public Optional<String> getLog() {
		return Optional.ofNullable(event);
	}

	public Optional<TTFlowWithGuard> getModel() {
		return Optional.ofNullable(flow);
	}

	public boolean isSkip() {
		return type == StepType.logmove;
	}

	public String toString() {
		if (type == StepType.sync) {
			return "("+ event
			+","+flow.toString()+")";
		} else {
			return "("+ event
			+","+MatchingStep.SKIPPER+")";
		}
	}

	public int hashCode() {
		return Objects.hash(event, flow, type);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CManyMatchingStep other = (CManyMatchingStep) obj;
		return Objects.equals(
				event, 
				other.event) 
				&& Objects.equals(flow, other.flow) 
				&& type == other.type;
	}
	
	

}
