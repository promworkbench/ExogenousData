package org.processmining.qut.exogenousdata.conformance;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.bookkeeping.TraversalBookkeepingEngine;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.CManyMatching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.Matching;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;

public class BookkeepingMeasures {
	
	private BookkeepingMeasures() {};
	
	public static double computeGuardRecall(XLog log, PetriNetWithData model) {
		return computeGuardRecall(
				log, new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
			)
		);
	}
	
	public static double computeGuardRecall(XLog log, PetriNetWithData model,
			UIPluginContext context) {
		context.getProgress().setMaximum(3);
		context.getProgress().setValue(0);
		context.getProgress().setMinimum(0);
		return computeGuardRecall(
				log, 
				new PNWDTransitionTree( 
					new PNWDPlayoutEngine(context).generateTraces(
							model, LoggyUtils.findLongestTrace(log)
					) 
				),
				context
		);
	}
	
	public static double computeGuardRecall(XLog log, PNWDTransitionTree tree) {
		Matching matcher = new CManyMatching(tree);
		Double ret = new TraversalBookkeepingEngine()
				.computeBookkeeping(tree, matcher, log);
		return ret;
	}
	
	public static double computeGuardRecall(XLog log, PNWDTransitionTree tree,
			UIPluginContext context) {
		synchronized (context) {
			Progress prog = context.getProgress();
			prog.setMaximum(prog.getMaximum() + log.size());
			context.log("Working on computing guard-recall.");
		}
		Matching matcher = new CManyMatching(tree);
		Double ret = new TraversalBookkeepingEngine(context)
				.computeBookkeeping(tree, matcher, log);
		return ret;
	}
	
	public static double computeGuardPrecision(XLog log, PetriNetWithData model) {
		return computeGuardPrecision(
				log, new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
			)
		);
	}
	
	public static double computeGuardPrecision(XLog log, PetriNetWithData model,
			UIPluginContext context
			) {
		synchronized (context) {
			context.getProgress().setMaximum(3);
			context.getProgress().setValue(0);
			context.getProgress().setMinimum(0);
		}
		return computeGuardPrecision(
				log, 
				new PNWDTransitionTree( 
					new PNWDPlayoutEngine(context).generateTraces(
							model, LoggyUtils.findLongestTrace(log)
				)), 
				context
		);
	}
	
	public static double computeGuardPrecision(XLog log, PNWDTransitionTree tree) {
		return 0.0;
	}
	
	public static double computeGuardPrecision(XLog log, PNWDTransitionTree tree,
			UIPluginContext context) {
		Progress prog = context.getProgress();
		prog.setMaximum(prog.getMaximum() + log.size() * 2);
		context.log("Working on computing guard-precision.");
		Matching matcher = new CManyMatching(tree);
		
		return 0.0;
	}


}
