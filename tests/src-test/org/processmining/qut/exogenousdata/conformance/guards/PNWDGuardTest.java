package org.processmining.qut.exogenousdata.conformance.guards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.guards.Guard.GuardOutcomes;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;

public class PNWDGuardTest {
	
	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_3.pnml");

	@Test
	public void test() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, 5));
		
		Map<String, Object> datastateT = new HashMap() {{
			put("d1", 3);
		}};
		Map<String,Object> datastateF = new HashMap() {{ 
			put("d1", 12);
		}};
		Map<String,Object> datastateU = new HashMap() {{
			put("x", "xsa");
		}};
		
		for(TTFlowWithGuard flow : tree.getFlows()) {
			if (!flow.hasGuard()) {
				assertEquals(GuardOutcomes.TRUE, flow.evaluateGuard(datastateT));
				assertEquals(GuardOutcomes.TRUE, flow.evaluateGuard(datastateF));
				assertEquals(GuardOutcomes.TRUE, flow.evaluateGuard(datastateU));
				continue;
			}
			assertEquals(GuardOutcomes.TRUE, flow.evaluateGuard(datastateT));
			GuardOutcomes ret = flow.evaluateGuard(datastateF);
			if (ret == GuardOutcomes.UNDEF) {
				fail("guard should not evaluate undefined.");
			}
			ret = flow.evaluateGuard(datastateU);
			if (ret == GuardOutcomes.FALSE) {
				fail("guard should not evaluate to false.");
			}
		}
	}

}
