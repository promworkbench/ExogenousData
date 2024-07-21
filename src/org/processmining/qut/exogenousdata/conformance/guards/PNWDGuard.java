package org.processmining.qut.exogenousdata.conformance.guards;

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
	
	
}
