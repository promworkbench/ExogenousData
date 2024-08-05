package org.processmining.qut.exogenousdata.conformance.transitiontree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Test;
import org.processmining.log.utils.XLogBuilder;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;

public class CManyMatchingTest {
	
	Path modelPath = Paths.get(
			"tests","src-test","resource", "ax3_model_3.pnml");
	Path modelReworkPath = Paths.get(
			"tests","src-test","resource", "testing_dpn_with_rework.pnml");

	@Test
	public void simpleTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		PNWDTransitionTree tree = new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(model, 8));
		
		XLog log = XLogBuilder.newInstance()
					.startLog("dummy")
					.addTrace("T1")
					.addEvent("A")
					.addEvent("B")
					.addEvent("Z")
					.build();
		XTrace trace = log.get(0);
		
		CManyMatching matcher = new CManyMatching(tree);
		
		for(Iterable<MatchingStep<XEvent, TTFlowWithGuard>> path : 
			matcher.getAllPaths(log.get(0))) {
			int cost = matcher.computeCost(path, trace);
			System.out.println("Returned path ::"
					+ path
					+ ", cost of :: "
					+ cost);
			if (cost <1) {
				fail("Should not return a cost less than one, it cannot be optimal.");
			}
		}
	}
	
	@Test
	public void paperExampleTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelReworkPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		PNWDTransitionTree tree = new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(model, 4));
		
		XLog log = XLogBuilder.newInstance()
					.startLog("dummy")
					.addTrace("T1")
					.addEvent("a")
					.addEvent("b")
					.addEvent("z")
					.build();
		XTrace trace = log.get(0);
		
		CManyMatching matcher = new CManyMatching(tree);
		
		int count = 0;
		for(Iterable<MatchingStep<XEvent, TTFlowWithGuard>> path : 
			matcher.getAllPaths(log.get(0))) {
			int cost = matcher.computeCost(path, trace);
			System.out.println("Returned path ::"
					+ path
					+ ", cost of :: "
					+ cost);
			if (cost != 2) {
				fail("Should only return paths with a cost of 2.");
			}
			count++;
		}
		
		if (count != 7) {
			assertEquals(7, count);
		}
	}
	
	@Test
	public void optimalTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		PNWDTransitionTree tree = new PNWDTransitionTree(
				new PNWDPlayoutEngine().generateTraces(model, 8));
		
		XLog log = XLogBuilder.newInstance()
					.startLog("dummy")
					.addTrace("T1")
					.addEvent("A")
					.addEvent("B")
					.addEvent("E")
					.addEvent("F")
					.build();
		XTrace trace = log.get(0);
		
		CManyMatching matcher = new CManyMatching(tree);
		
		for(Iterable<MatchingStep<XEvent, TTFlowWithGuard>> path : 
			matcher.getAllPaths(log.get(0))) {
			int cost = matcher.computeCost(path, trace);
			System.out.println("Returned path ::"
					+ path
					+ ", cost of :: "
					+ cost);
			if (cost > 0) {
				fail("Should not return a cost of more than zero, it has an optimal path.");
			}
		}
	}

}
