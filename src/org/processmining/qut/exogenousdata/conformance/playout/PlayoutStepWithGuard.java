package org.processmining.qut.exogenousdata.conformance.playout;

import org.processmining.qut.exogenousdata.conformance.guards.Guard;

public class PlayoutStepWithGuard implements PlayoutStep {
	
	private boolean halt;
	private String label;
	private Guard guard;
	
	public PlayoutStepWithGuard(boolean halt, String label, Guard guard) {
		super();
		this.halt = halt;
		this.label = label;
		this.guard = guard;
	}

	public String toString() {
		if (isHalt()) {
			return "PlayoutStepWithGuard [HALTING]";
		} else {
			return "PlayoutStepWithGuard [label=" + label + ", guard=" + guard.toString() + "]";
		}
	}

	public boolean isHalt() {
		return halt;
	}

	public String getLabel() {
		return label;
	}

	public Guard getGuard() {
		return guard;
	}
	
}
