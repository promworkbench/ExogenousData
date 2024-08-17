package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Map;
import java.util.Objects;

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
		return Objects.hash(label, guard.getID(), src.getVariant(), tgt.getVariant());
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TTFlowWithGuard other = (TTFlowWithGuard) obj;
		return 
				Objects.equals(label, other.label) 
				&& Objects.equals(src, other.src) 
				&& Objects.equals(guard.getID(), other.guard.getID()) 
				&& Objects.equals(tgt, other.tgt);
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
