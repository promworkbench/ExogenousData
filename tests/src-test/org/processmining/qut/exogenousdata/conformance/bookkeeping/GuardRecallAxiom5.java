package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Test;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.CManyMatching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.MatchingStep;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.conformance.transitiontree.TTFlowWithGuard;
import org.processmining.qut.exogenousdata.utils.LoadyUtils;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;
import org.processmining.qut.exogenousdata.utils.TraceyUtils;

public class GuardRecallAxiom5 {
	
	Path axiomDir = Paths.get(
			"tests","src-test","resource", "axioms", "axiom 5"
	);
	
	Path logPath = Paths.get(axiomDir.toString(), "log_1.xes");
	Path modelPath = Paths.get(axiomDir.toString(), "ax5_model_1.pnml");
	
	@Test
	public void testAxiom() {
		XLog log = LoadyUtils.loadLogFromFile(logPath.toFile());
		if (log == null) {
			fail("Unable to load in testing log file for event log.");
		}
		PetriNetWithData model = LoadyUtils.loadDPNFromFile(
				modelPath.toFile()
		);
		if (model == null) {
			fail("Unable to load in testing pnml file for DPN.");
		}
		
		
		List<Double> scores = new ArrayList<>();
		PNWDTransitionTree tree = new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
		);
		double ret;
		TraversalBookkeepingEngine engine = new TraversalBookkeepingEngine();
		Map<XTrace, Iterable<List<MatchingStep<String, TTFlowWithGuard>>>> 
			precompute;
		Matching dmatcher = new CManyMatching(tree);
//		create matching which returns shorter paths.
		precompute = new HashMap<>();
		for(List<String> variant: LoggyUtils.getAllVariants(log)) {
			XTrace shrtdummy = TraceyUtils.createDummyVariant(
					variant.subList(0, variant.size()-1).toArray(new String[0]));
			XTrace dummy = TraceyUtils.createDummyVariant(
					variant.toArray(new String[0]));
			precompute.put(dummy, dmatcher.getAllPaths(shrtdummy));
		}
		Matching wrstmatcher = new CManyMatching(tree, precompute);
		ret = engine.computeBookkeeping(tree, wrstmatcher, log);
		System.out.println(
				"[Axiom 5] Computed guard-recall ("
				+"using shorter paths"
				+") was :: " 
				+ ret);
		scores.add(ret);
//		create matching which return the similar paths for second choice
		precompute = new HashMap<>();
		for(List<String> variant: LoggyUtils.getAllVariants(log)) {
			List<String> nvariant = new ArrayList();
			nvariant.addAll(variant.subList(0, variant.size()-1));
			nvariant.add("F");
			XTrace otherdummy = TraceyUtils.createDummyVariant(
					nvariant.toArray(new String[0]));
			XTrace dummy = TraceyUtils.createDummyVariant(
					variant.toArray(new String[0]));
			precompute.put(dummy, dmatcher.getAllPaths(otherdummy));
		}
		Matching midmatcher = new CManyMatching(tree, precompute);
		ret = engine.computeBookkeeping(tree, midmatcher, log);
		System.out.println(
				"[Axiom 5] Computed guard-recall ("
				+"using similar worse paths"
				+") was :: " 
				+ ret);
		scores.add(ret);
//		create least costy matching
		Matching lcmatcher = new CManyMatching(tree);
		ret = engine.computeBookkeeping(tree, lcmatcher, log);
		System.out.println(
				"[Axiom 5] Computed guard-recall ("
				+"using all paths"
				+") was :: " 
				+ ret);
		scores.add(ret);
		
		for(int i=0; i <scores.size()-1; i++) {
			for(int j=i+1; j < scores.size(); j++) {
				if (scores.get(i) > scores.get(j)) {
					fail(
						"[Axiom 5] The computed guard-recall for worsening matchings "
						+ i 
						+ " ("
						+ scores.get(i) 
						+ ") was larger than expected, comparing to more complete matchings "
						+ j
						+ " ("
						+ scores.get(j)
						+ ")"
					);
				}
			}
		}
	}

}
