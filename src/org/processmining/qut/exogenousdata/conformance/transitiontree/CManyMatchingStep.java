package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Optional;

import org.deckfour.xes.model.XEvent;

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

}
