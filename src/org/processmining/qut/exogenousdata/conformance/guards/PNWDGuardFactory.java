package org.processmining.qut.exogenousdata.conformance.guards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;

public class PNWDGuardFactory {
	
	private PNWDGuardFactory() {		
	}
	
	private static Map<String, Integer> storage = new HashMap<>();
	private static int curr = 0;
	
	public static PNWDGuard getGuard(PNWDTransition trans) {
		String guard = trans.getGuardAsString();
//		check for id 
		if (!storage.containsKey(guard)) {
			curr += 1;
			storage.put(guard, curr);
		}
		int id = storage.get(guard);
//		return guard
		return new PNWDGuard(
				trans.getGuardExpression(),
				id
		);
	}
	
	public static Weakening<Guard> getWeakening(
			List<PNWDTransition> trans) {
		return new NaiveWeakening();
	}
	
	public static Strengthening<Guard> getStrengthening(
			List<PNWDTransition> trans){
		return new NaiveStrengthening();
	}

}
