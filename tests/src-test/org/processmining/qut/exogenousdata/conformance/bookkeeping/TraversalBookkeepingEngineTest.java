package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.CManyMatching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;

public class TraversalBookkeepingEngineTest {
	
	Path modelFullPath = Paths.get(
			"tests","src-test","resource", "test_dpn_full_grec.pnml");
	Path modelEasyPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_c.pnml");
	Path modelMidPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_b.pnml");
	Path modelHardPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_a.pnml");
	Path logPath = Paths.get( 
			"tests","src-test","resource", "paper_example_log.xes");

	@Test
	public void traversalBookkeepingOnFlowsTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelEasyPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
		PNWDTransitionTree tree = new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
		);
		Matching matcher = new CManyMatching(tree);
		for(TTFlowWithGuard flow : tree.getFlows()) {
			double ret = engine.computeBookkeeping(flow, matcher, log);
			System.out.println("Computed traversal booking for "
					+ flow
					+ " was "
					+ ret
			);
			assertEquals(true, ret > 0);
		}
	}
	
	@Test
	public void computeTraversalBookingHardTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelHardPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
		PNWDTransitionTree tree = new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
		);
		Matching matcher = new CManyMatching(tree);
		double ret = engine.computeBookkeeping(tree, matcher, log);
		System.out.println("Computed guard-recall (hard) was :: " + ret);
	}
	
	@Test
	public void computeTraversalBookingMidTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelMidPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
		PNWDTransitionTree tree = new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
		);
		Matching matcher = new CManyMatching(tree);
		double ret = engine.computeBookkeeping(tree, matcher, log);
		System.out.println("Computed guard-recall (mid) was :: " + ret);
	}
	
	@Test
	public void computeTraversalBookingFullTest() {
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(modelFullPath.toFile());
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
		PNWDTransitionTree tree = new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
		);
		Matching matcher = new CManyMatching(tree);
		double ret = engine.computeBookkeeping(tree, matcher, log);
		System.out.println("Computed guard-recall (full) was :: " + ret);
		assertEquals(1.0, ret, 1e-4);
	}

}
