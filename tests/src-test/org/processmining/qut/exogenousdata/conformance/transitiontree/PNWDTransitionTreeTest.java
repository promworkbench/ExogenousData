package org.processmining.qut.exogenousdata.conformance.transitiontree;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;

public class PNWDTransitionTreeTest {

	Path modelPath = Paths.get(
			"tests","src-test","resource", "axioms", "axiom 3",  "ax3_model_3.pnml");
	Path modelReworkPath = Paths.get(
			"tests","src-test","resource", "testing_dpn_with_rework.pnml");
	
	@Test
	public void constructionTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, 5));
//		System.out.println(tree);
	}
	
	@Test
	public void attemptVariousConstructionsTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(int n=0; n < 6; n++) {
			try {
			PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, n));
			} catch (Exception e) {
				fail("Unable to build tree with n="+n+"\n stack::"+e.getStackTrace().toString());
			}
		}
	}
	
	@Test
	public void attemptVariousConstructionsWithReworkTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelReworkPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		for(int n=0; n < 4; n++) {
			try {
			PNWDTransitionTree tree = new PNWDTransitionTree(new PNWDPlayoutEngine().generateTraces(model, n));
//			System.out.println("n="+n+"\n"+tree.toString());
			} catch (Exception e) {
				fail("Unable to build tree with n="+n+"\n stack::"+e.getStackTrace().toString());
			}
			
		}
	}
	
	
	@Test
	public void attemptLongConstructionsWithReworkTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(
				modelReworkPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		try {
		PNWDTransitionTree tree = new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(model, 7));
		} catch (Exception e) {
			fail("Unable to build a deep tree with stack::"
					+e.getStackTrace().toString());
		}
			
	}
	@Test
	public void ensureCorrectNodeTests() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelReworkPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		PNWDTransitionTree tree = new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(model, 4));
		if (tree.getNodes().size() != 32) {
			fail("Expected 31 nodes after unfolding all variants,"
					+ " returned ::"+tree.getNodes().size());
		}
	}
	
	

}
