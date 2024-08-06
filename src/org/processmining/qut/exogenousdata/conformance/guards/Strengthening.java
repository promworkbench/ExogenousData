package org.processmining.qut.exogenousdata.conformance.guards;

public interface Strengthening<T extends Guard> extends Guard {
	
	public final String OPERATOR = "&#x2295;";
	
	abstract public int numOfGuards();

}
