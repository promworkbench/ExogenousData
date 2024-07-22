package org.processmining.qut.exogenousdata.conformance.transitiontree;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.utils.LoadingUtils;

public class PNWDTransitionTreeTest {

	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_3.pnml");
	
	@Test
	public void constructionTest() {
		PetriNetWithData model = LoadingUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, 5));
		System.out.println(tree);
	}
	
	@Test
	public void attemptVariousConstructionsTest() {
		PetriNetWithData model = LoadingUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(int n=0; n < 20; n++) {
			try {
			PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, n));
			} catch (Exception e) {
				fail("Unable to build tree with n="+n+"\n stack::"+e.getStackTrace().toString());
			}
		}
	}

}
