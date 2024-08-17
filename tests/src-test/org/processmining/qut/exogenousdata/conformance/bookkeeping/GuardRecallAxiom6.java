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

public class GuardRecallAxiom6 {
	
	Path axiomDir = Paths.get(
			"tests","src-test","resource", "axioms", "axiom 6"
	);
	
	Path modelPatha = Paths.get(axiomDir.toString(), "ax6_model_1.pnml");
	Path modelPathb = Paths.get(axiomDir.toString(), "ax6_model_2.pnml");
	Path modelPathc = Paths.get(axiomDir.toString(), "ax6_model_3.pnml");
	
	@Test
	public void testAxiomA() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(
				modelPatha.toFile()
		);
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		
		List<Double> scores = new ArrayList<>();
		
		for(int i=1; i < 4; i++) {
			double ret;
			XLog log = LoadyUtils.loadLogFromFile(
					Paths.get(axiomDir.toString(), "log_"+i+".xes").toFile()
					
			);
			if (log == null) {
				fail("Unable to load in testing log file for event log.");
			}
			PNWDTransitionTree tree = new PNWDTransitionTree( 
					new PNWDPlayoutEngine().generateTraces(
							model, LoggyUtils.findLongestTrace(log)
					) 
			);
			TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
			Matching matcher = new CManyMatching(tree);
			ret = engine.computeBookkeeping(tree, matcher, log);
			System.out.println(
					"[Axiom 6] Computed guard-recall ("
					+"using log "
					+ i
					+") was :: " 
					+ ret);
			scores.add(ret);
		}
		
		for(int i=0; i <scores.size()-1; i++) {
			for(int j=i+1; j < scores.size(); j++) {
				assertEquals(scores.get(i), scores.get(j), 1e-4);
			}
		}
	}
	
	@Test
	public void testAxiomB() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(
				modelPathb.toFile()
		);
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		
		List<Double> scores = new ArrayList<>();
		
		for(int i=1; i < 4; i++) {
			double ret;
			XLog log = LoadyUtils.loadLogFromFile(
					Paths.get(axiomDir.toString(), "log_"+i+".xes").toFile()
					
			);
			if (log == null) {
				fail("Unable to load in testing log file for event log.");
			}
			PNWDTransitionTree tree = new PNWDTransitionTree( 
					new PNWDPlayoutEngine().generateTraces(
							model, LoggyUtils.findLongestTrace(log)
					) 
			);
			TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
			Matching matcher = new CManyMatching(tree);
			ret = engine.computeBookkeeping(tree, matcher, log);
			System.out.println(
					"[Axiom 6] Computed guard-recall ("
					+"using log "
					+ i
					+") was :: " 
					+ ret);
			scores.add(ret);
		}
		
		for(int i=0; i <scores.size()-1; i++) {
			for(int j=i+1; j < scores.size(); j++) {
				assertEquals(scores.get(i), scores.get(j), 1e-4);
			}
		}
	}
	
	@Test
	public void testAxiomC() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(
				modelPathc.toFile()
		);
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		
		List<Double> scores = new ArrayList<>();
		
		for(int i=1; i < 4; i++) {
			double ret;
			XLog log = LoadyUtils.loadLogFromFile(
					Paths.get(axiomDir.toString(), "log_"+i+".xes").toFile()
					
			);
			if (log == null) {
				fail("Unable to load in testing log file for event log.");
			}
			PNWDTransitionTree tree = new PNWDTransitionTree( 
					new PNWDPlayoutEngine().generateTraces(
							model, LoggyUtils.findLongestTrace(log)
					) 
			);
			TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
			Matching matcher = new CManyMatching(tree);
			ret = engine.computeBookkeeping(tree, matcher, log);
			System.out.println(
					"[Axiom 6] Computed guard-recall ("
					+"using log "
					+ i
					+") was :: " 
					+ ret);
			scores.add(ret);
		}
		
		for(int i=0; i <scores.size()-1; i++) {
			for(int j=i+1; j < scores.size(); j++) {
				assertEquals(scores.get(i), scores.get(j), 1e-4);
			}
		}
	}

}
