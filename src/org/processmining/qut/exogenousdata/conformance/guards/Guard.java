package org.processmining.qut.exogenousdata.conformance.guards;

import java.util.Map;

public interface Guard {
	
	public static enum GuardOutcomes{
		TRUE,
		FALSE,
		UNDEF;
	}
	
	abstract public int getID();
	
	abstract public String cannonRepresentation();
	
	abstract public GuardOutcomes evaluate(Map<String,Object> datastate); 
	
	abstract public boolean isTrue();
	
	abstract public boolean isFalse();

}
