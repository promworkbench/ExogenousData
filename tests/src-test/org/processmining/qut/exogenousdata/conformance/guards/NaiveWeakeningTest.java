package org.processmining.qut.exogenousdata.conformance.guards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.guards.Guard.GuardOutcomes;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;

public class NaiveWeakeningTest {

	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_3.pnml");
	
	@Test
	public void keepGaurdsTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		PNWDTransition[] trans = 
				model.getTransitions().toArray(new PNWDTransition[0]);
		
//		try with one guard
		Guard g = PNWDGuardFactory.getGuard(trans[0]);
		
		Weakening<Guard> weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
				}}
		);
//		System.out.println(weak.toString());
		assertEquals(1, weak.numOfGuards());
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[0]);
				}}
		);
//		System.out.println(weak.toString());
		assertEquals(1, weak.numOfGuards());
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
				}}
		);
//		System.out.println(weak.toString());
		assertEquals(2, weak.numOfGuards());
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
					add(trans[2]);
					add(trans[2]);
				}}
		);
//		System.out.println(weak.toString());
		assertEquals(3, weak.numOfGuards());
	}
	
	@Test
	public void gaurdOutcomesTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		Map<String, Object> datastateT = new HashMap() {{
			put("d1", 3);
		}};
		Map<String,Object> datastateF = new HashMap() {{ 
			put("d1", 15);
		}};
		Map<String,Object> datastateU = new HashMap() {{
			put("x", "xsa");
		}};
		
		PNWDTransition[] trans = 
				model.getTransitions().toArray(new PNWDTransition[0]);
		
//		try with one guard
		Guard g = PNWDGuardFactory.getGuard(trans[0]);
		
		Weakening<Guard> weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
				}}
		);
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateU));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateT));
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[0]);
				}}
		);
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateU));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateT));
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
				}}
		);
//		System.out.println(weak.cannonRepresentation());
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateU));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateT));
		weak = PNWDGuardFactory.getWeakening( 
				new ArrayList(){{ 
					add(trans[1]);
					add(trans[2]);
					add(trans[3]);
				}}
		);
//		System.out.println(weak.cannonRepresentation());
		assertEquals(3, weak.numOfGuards());
		assertEquals(GuardOutcomes.UNDEF, weak.evaluate(datastateU));
		assertEquals(GuardOutcomes.FALSE, weak.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, weak.evaluate(datastateT));
	}
	

}
