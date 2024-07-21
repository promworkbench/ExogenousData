package org.processmining.qut.exogenousdata.conformance.playout;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.qut.exogenousdata.conformance.guards.PNWDGuardFactory;

public class PlayoutTraceWithGuards 
	implements PlayoutTrace<PlayoutStepWithGuard> {
	
	private List<PlayoutStepWithGuard> steps;
	
	public PlayoutTraceWithGuards(List<PNWDTransition> history) {
		super();
		this.steps = new ArrayList<>();
		processHistory(history);
	}

	
	public void processHistory(List<PNWDTransition> history) {
		List<PNWDTransition> silents = new ArrayList();
		for(PNWDTransition trans: history) {
			if (trans.isInvisible()) {
				silents.add(trans);
			} else {
				if (silents.size() < 1) {
					steps.add(
						new PlayoutStepWithGuard(
								false, 
								trans.getLabel(), 
								PNWDGuardFactory.getGuard(trans)
						)
					);
				} else {
					silents.add(trans);
					steps.add( 
						new PlayoutStepWithGuard(false,
								trans.getLabel(),
								PNWDGuardFactory.getStrengthening(silents)
						)	
					);
					silents.clear();
				}
			}
		}
	}


	public Iterable<PlayoutStepWithGuard> getSteps() {
		// TODO Auto-generated method stub
		return steps;
	}


	public String toString() {
		return "PlayoutTraceWithGuards("
				+ steps.size()
				+") [steps=" + steps + "]";
	}
	
	

}
