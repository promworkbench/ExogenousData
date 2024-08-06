package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Map;

import org.processmining.qut.exogenousdata.conformance.guards.Guard;
import org.processmining.qut.exogenousdata.conformance.guards.Guard.GuardOutcomes;
import org.processmining.qut.exogenousdata.conformance.guards.PNWDGuard;

public class TTFlowWithGuard implements TransitionTreeFlow {

	private String label;
	private Guard guard;
	private TTNode src;
	private TTNode tgt;
	
	public TTFlowWithGuard(String label, Guard guard, TTNode src, TTNode tgt) {
		super();
		this.label = label;
		this.guard = guard;
		this.src = src;
		this.tgt = tgt;
	}
	
	public boolean hasGuard() {
		if (guard instanceof PNWDGuard) {
			PNWDGuard g = (PNWDGuard) guard;
			return g.getGuard() != null;
		} else {
			return guard != null;
		}
		
	}

	public TTNode src() {
		return src;
	}

	public TTNode tgt() {
		return tgt;
	}

	public String label() {
		return label;
	}
	
	public Guard guard() {
		return guard;
	}
	
	public GuardOutcomes evaluateGuard(Map<String,Object> datastate) {
		return guard.evaluate(datastate);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((guard == null) ? 0 : guard.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((tgt == null) ? 0 : tgt.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TTFlowWithGuard other = (TTFlowWithGuard) obj;
		if (guard == null) {
			if (other.guard != null)
				return false;
		} else if (!guard.equals(other.guard))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (tgt == null) {
			if (other.tgt != null)
				return false;
		} else if (!tgt.equals(other.tgt))
			return false;
		return true;
	}
	
	public String toString() {
		String guardString = " ";
		try {
			guardString = guard.cannonRepresentation();
		} catch (Exception e) {
//			expected fail
		}
		
		return "TTFlowWithGuard [ "
			+ src.getVariant()
			+ "->"
			+ guardString
			+ "->"
			+ tgt.getVariant();
	}

}
