package org.processmining.qut.exogenousdata.stochastic.replayer;

import java.util.BitSet;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.stochastic.conformance.ExogenousDataState;
import org.processmining.qut.exogenousdata.stochastic.conformance.ExogenousDataStateLogAdapter;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.xeslite.common.XUtils;

public class SLPNEDReplayer {
	
	public SLPNEDReplayer() {};
	
	public void replay(StochasticLabelledPetriNetWithExogenousData net,
			ExogenousAnnotatedLog xlog) {
		SLPNEDSemantics semantics = net.getDefaultSemantics();
		ExogenousDataStateLogAdapter adapter = new ExogenousDataStateLogAdapter(
				semantics, 
				(ExogenousDataset[]) xlog.getExogenousDatasets().toArray()
		);
		for(XTrace trace: xlog) {
			replayTrace(adapter, semantics.clone(), trace);
		}
		
	}
	
	public void replayTrace(ExogenousDataStateLogAdapter adapter,
			SLPNEDSemantics semantics, XTrace trace) {
		
		for(XEvent event : trace) {
			if (semantics.isFinalState()) {
				System.out.println("[SLPNEDReplayer] in final state but still "
						+ "had more events.");
				break;
			}
//			find a transition to fire
			String concept = XUtils.getConceptName(event);
			int transition = -1;
			int guess = -1;
			BitSet enabled = semantics.getEnabledTransitions();
			for (int pos= enabled.nextSetBit(0);
					pos != -1; pos = enabled.nextSetBit(pos+1)) {
				guess = pos; 
				if (concept.equals(semantics.getTransitionLabel(transition))) {
					transition = pos;
					break;
				}
			}
			if (transition < 0) {
				transition = guess;
			}
			DataState state = adapter.fromEvent(event);
			if (state instanceof ExogenousDataState) {
				ExogenousDataState xstate = (ExogenousDataState) state;
				debugState(semantics, transition, xstate);
				semantics.executeTransition(transition);
			}
		}
	}
	
	public void replayTrace(ExogenousDataStateLogAdapter adapter,
			SLPNEDSemantics semantics, XTrace trace,
			int[] firing) throws Exception {
		int fireId = 0;
		for(XEvent event : trace) {
			if (fireId >= firing.length) {
				System.out.println("Ending firing but still had events lefts.");
				break;
			}
//			find a transition to fire
			int transition = 0;
			BitSet enabled = semantics.getEnabledTransitions();
			if (!enabled.get(transition)) {
				throw new Exception("Could not fire transition :: "
						+transition
						+" in state :: "
						+semantics.toString());
			}
			DataState state = adapter.fromEvent(event);
			if (state instanceof ExogenousDataState) {
				ExogenousDataState xstate = (ExogenousDataState) state;
				debugState(semantics, transition, xstate);
				semantics.executeTransition(transition);
			}
			fireId += 1;
		}
	}
	
	public void debugState(SLPNEDSemantics semantics, int transition,
			ExogenousDataState xstate) {
		System.out.println("Firing transition :: "
				+semantics.getTransitionLabel(transition));
		
	}

}
