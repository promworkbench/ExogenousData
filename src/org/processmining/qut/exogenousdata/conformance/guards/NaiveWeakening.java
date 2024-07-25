package org.processmining.qut.exogenousdata.conformance.guards;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveWeakening implements Weakening<Guard> {
	
	private Set<Guard> contains;
	
	public NaiveWeakening(List<Guard> options) {
		contains = new HashSet<>();
		for(Guard g : options) {
			contains.add(g);
		}
	}

	public int getID() {
		// TODO Auto-generated method stub
		return hashCode();
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
				ret += "(" + g.cannonRepresentation() +")"+ OPERATOR;
			}
		}
		if (ret.length() > OPERATOR.length()) {
			ret = ret.substring(0, ret.length() - OPERATOR.length());
		}
		return ret;
	}

	public GuardOutcomes evaluate(Map<String, Object> datastate) {
		// TODO Auto-generated method stub
		return GuardOutcomes.FALSE;
	}

	public boolean isTrue() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isFalse() {
		// TODO Auto-generated method stub
		return false;
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
		NaiveWeakening other = (NaiveWeakening) obj;
		if (contains == null) {
			if (other.contains != null)
				return false;
		} else if (!contains.equals(other.contains))
			return false;
		return true;
	}
	
	

}
