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
import org.processmining.qut.exogenousdata.utils.LoadingUtils;

public class NaiveStrengtheningTest {
	
	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_3.pnml");

	@Test
	public void keepGaurdsTest() {
		PetriNetWithData model = LoadingUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		PNWDTransition[] trans = 
				model.getTransitions().toArray(new PNWDTransition[0]);
		
//		try with one guard
		Guard g = PNWDGuardFactory.getGuard(trans[0]);
		
		Strengthening<Guard> strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
				}}
		);
		System.out.println(strength.toString());
		assertEquals(1, strength.numOfGuards());
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[0]);
				}}
		);
		System.out.println(strength.toString());
		assertEquals(1, strength.numOfGuards());
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
				}}
		);
		System.out.println(strength.toString());
		assertEquals(2, strength.numOfGuards());
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
					add(trans[2]);
				}}
		);
		System.out.println(strength.toString());
		assertEquals(3, strength.numOfGuards());
	}
	
	@Test
	public void gaurdOutcomesTest() {
		PetriNetWithData model = LoadingUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		Map<String, Object> datastateT = new HashMap() {{
			put("d1", 3);
		}};
		Map<String,Object> datastateF = new HashMap() {{ 
			put("d1", 9);
		}};
		Map<String,Object> datastateU = new HashMap() {{
			put("x", "xsa");
		}};
		
		PNWDTransition[] trans = 
				model.getTransitions().toArray(new PNWDTransition[0]);
		
//		try with one guard
		Guard g = PNWDGuardFactory.getGuard(trans[0]);
		
		Strengthening<Guard> strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
				}}
		);
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateU));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateT));
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[0]);
				}}
		);
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateU));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateT));
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
				}}
		);
		assertEquals(GuardOutcomes.UNDEF, strength.evaluate(datastateU));
		assertEquals(GuardOutcomes.FALSE, strength.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateT));
		strength = PNWDGuardFactory.getStrengthening( 
				new ArrayList(){{ 
					add(trans[0]);
					add(trans[1]);
					add(trans[2]);
				}}
		);
		assertEquals(3, strength.numOfGuards());
		assertEquals(GuardOutcomes.UNDEF, strength.evaluate(datastateU));
		assertEquals(GuardOutcomes.FALSE, strength.evaluate(datastateF));
		assertEquals(GuardOutcomes.TRUE, strength.evaluate(datastateT));
	}

}
