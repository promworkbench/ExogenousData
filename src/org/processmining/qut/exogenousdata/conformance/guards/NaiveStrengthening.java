package org.processmining.qut.exogenousdata.conformance.guards;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveStrengthening implements Strengthening<Guard> {
	
	private Set<Guard> contains;
	
	public NaiveStrengthening() {
		this.contains = new HashSet<>();
	}
	
	public NaiveStrengthening(List<Guard> options) {
		assert options.size() > 0;
		this.contains = new HashSet();
		for( Guard guard: options) {
			if (contains.size() > 0 && !guard.isTrue()) {
				contains.add(guard);
			} else {
				contains.add(guard);
			}
		}
		assert contains.size() > 0;
	}

	public int getID() {
		return 1;
	}

	public String cannonRepresentation() {
		String ret = "";
		if (contains.size() < 2) {
			for(Guard g : contains) {
				ret += g.cannonRepresentation();
			}
			return ret;
		} else {
			for(Guard g : contains) {
				ret += g.cannonRepresentation() + OPERATOR;
			}
		}
		if (ret.length() > OPERATOR.length()) {
			ret = ret.substring(0, ret.length() - OPERATOR.length());
		}
		return ret;
	}

	public GuardOutcomes evaluate(Map<String, Object> datastate) {
		GuardOutcomes ret = GuardOutcomes.TRUE;
		boolean allTruth = true;
		for(Guard guard : this.contains) {
			GuardOutcomes out = guard.evaluate(datastate);
			if (out == GuardOutcomes.FALSE) {
				return out;
			} else if (out == GuardOutcomes.UNDEF) {
				return out;
			}
		}
		return ret;
	}

	public String toString() {
		return "NaiveStrengthening [contains=" + contains + "]";
	}
	
	public int numOfGuards() {
		return contains.size();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contains == null) ? 0 : contains.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NaiveStrengthening other = (NaiveStrengthening) obj;
		if (contains == null) {
			if (other.contains != null)
				return false;
		} else if (!contains.equals(other.contains))
			return false;
		return true;
	}

	public boolean isTrue() {
		boolean ret = true;
		for(Guard g: contains) {
			ret = ret && g.isTrue();
		}
		return ret;
	}

	public boolean isFalse() {
		boolean ret = true;
		for(Guard g: contains) {
			ret = ret && g.isFalse();
		}
		return ret;
	}
	
}
