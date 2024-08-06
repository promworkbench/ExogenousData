package org.processmining.qut.exogenousdata.conformance;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.conformance.playout.PNWDPlayoutEngine;
import org.processmining.qut.exogenousdata.conformance.transitiontree.PNWDTransitionTree;
import org.processmining.qut.exogenousdata.utils.LoggyUtils;

public class BookkeepingMeasures {
	
	private BookkeepingMeasures() {};
	
	public double computeGuardRecall(XLog log, PetriNetWithData model) {
		
		return computeGuardRecall(
				log, new PNWDTransitionTree( 
				new PNWDPlayoutEngine().generateTraces(
						model, LoggyUtils.findLongestTrace(log)
				) 
			)
		);
	}
	
	public double computeGuardRecall(XLog log, PNWDTransitionTree tree) {
		return 0.0;
	}
	
	public double computeGuardPrecision(XLog log, PetriNetWithData model) {
		return 0.0;
	}
	
	public double computeGuardPrecision(XLog log, PNWDTransitionTree tree) {
		return 0.0;
	}

}
