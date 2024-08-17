package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.CManyMatching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;

public class GuardRecallAxiom4 {
	
	Path axiomDir = Paths.get(
			"tests","src-test","resource", "axioms", "axiom 4"
	);
	
	Path logPath = Paths.get(axiomDir.toString(), "log_1.xes");
	

	@Test
	public void testAxiom() {
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		
		List<Double> scores = new ArrayList<>();
		
		for(int i=1; i<4; i++) {
			PetriNetWithData model = LoadyUtils.loadDPNFromFile(
					Paths.get(
							axiomDir.toString(), 
							"ax4_model_"+i+".pnml"
					).toFile()
			);
			if (model == null) {
				fail("Unable to load in testing pnml file for DPN.");
			}
			TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
			PNWDTransitionTree tree = new PNWDTransitionTree( 
					new PNWDPlayoutEngine().generateTraces(
							model, LoggyUtils.findLongestTrace(log)
					) 
			);
			Matching matcher = new CManyMatching(tree);
			double ret = engine.computeBookkeeping(tree, matcher, log);
			System.out.println(
					"[Axiom 4] Computed guard-recall ("
					+i
					+") was :: " 
					+ ret);
			scores.add(ret);
		}
		
		for(int i=0; i <scores.size(); i++) {
			assertEquals(0.0, scores.get(i), 1e-4);
		}
	}

}
