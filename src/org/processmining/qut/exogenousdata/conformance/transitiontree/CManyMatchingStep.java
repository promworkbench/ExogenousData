package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Objects;
import java.util.Optional;

import org.deckfour.xes.model.XEvent;
import org.processmining.qut.exogenousdata.utils.EventyUtils;

public class CManyMatchingStep implements MatchingStep<XEvent, TTFlowWithGuard> {
	
	private XEvent event;
	private TTFlowWithGuard flow;
	private StepType type;
	
	public CManyMatchingStep(XEvent event, TTFlowWithGuard flow) {
		super();
		this.event = event;
		this.flow = flow;
		this.type = StepType.sync;
	}
	
	public CManyMatchingStep(XEvent event) {
		super();
		this.event = event;
		this.type = StepType.logmove;
	}

	public Optional<XEvent> getLog() {
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
			return "("+ EventyUtils.getConcept(event) 
			+","+flow.toString()+")";
		} else {
			return "("+ EventyUtils.getConcept(event) 
			+","+MatchingStep.SKIPPER+")";
		}
	}

	public int hashCode() {
		return Objects.hash(EventyUtils.getConcept(event), flow, type);
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
				EventyUtils.getConcept(event), 
				EventyUtils.getConcept(other.event)) 
				&& Objects.equals(flow, other.flow) 
				&& type == other.type;
	}
	
	

}
