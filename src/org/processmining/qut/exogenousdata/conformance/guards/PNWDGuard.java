package org.processmining.qut.exogenousdata.conformance.guards;

import java.util.Map;

import org.processmining.datapetrinets.exception.EvaluatorException;
import org.processmining.datapetrinets.expression.GuardExpression;

public class PNWDGuard implements Guard {
	
	private GuardExpression guard;
	private int ID;
	
	public PNWDGuard(GuardExpression guard, int iD) {
		super();
		this.guard = guard;
		ID = iD;
	}

	public GuardExpression getGuard() {
		return guard;
	}

	public int getID() {
		return ID;
	}

	public String toString() {
		if (guard != null) {
			return "PNWDGuard [guard=" + guard.toCanonicalString() + ", ID=" + ID + "]";
		} else {
			return "PNWDGuard [guard=" + "TRUE" + ", ID=" + ID + "]";
		}
	}

	public String cannonRepresentation() {
		// TODO Auto-generated method stub
		if (guard != null) {
			return guard.toCanonicalString();
		}
		return "gT";
	}

	public GuardOutcomes evaluate(Map<String, Object> datastate) {
		if (guard == null) {
			return GuardOutcomes.TRUE;
		}
		try {
			boolean eval = (boolean) guard.evaluate(datastate);
	//		System.out.println("Testing on datastate :: "
	//				+ datastate.toString() 
	//				+" result was "
	//				+ eval
	//				+ " for guard :: "
	//				+ guard.toCanonicalString());
			if (eval == true) {
				return GuardOutcomes.TRUE;
			} else if (eval == false){
				return GuardOutcomes.FALSE;
			}
		} catch (EvaluatorException e) {
//			Expected failure when evaluation does not have attributes in datastate
//			System.out.println("Failed to evaluate guard ::"
//					+ guard.toCanonicalString()
//					+ " using datastate :: "
//					+ datastate.toString());
		}
		return GuardOutcomes.UNDEF;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PNWDGuard other = (PNWDGuard) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	public boolean isTrue() {
		if (guard != null) {
			return guard.isTrue();
		}
		return true;
	}

	public boolean isFalse() {
		if (guard != null) {
			return guard.isFalse();
		}
		return false;
	}
	
	
	
	
}
