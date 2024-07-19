package org.processmining.qut.exogenousdata.stochastic.replayer;

import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Map.Entry;

import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.data.ExogenousUtils;
import org.processmining.qut.exogenousdata.stochastic.conformance.ExogenousDataState;
import org.processmining.qut.exogenousdata.stochastic.conformance.ExogenousDataStateLogAdapter;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.qut.exogenousdata.stochastic.model.StochasticLabelledPetriNetWithExogenousData;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.xeslite.common.XUtils;

import net.sf.saxon.exslt.Math;

public class SLPNEDReplayer {
	
//	state
	private String INTERESTED_TLABEL = null;
	private String INTERESTED_CASE = null;
	private int[] FIRING_SEQ = null;
	
	public SLPNEDReplayer() {};
	
	public SLPNEDReplayer setInterestCase(String concept) {
		INTERESTED_CASE = concept;
		return this;
	}
	
	public SLPNEDReplayer setInterestedTransitionLabel(String label) {
		this.INTERESTED_TLABEL = label;
		return this;
	}
	
	public SLPNEDReplayer setFiringSeq(int[] firing) {
		FIRING_SEQ = firing;
		return this;
	}
	
//	internals
	private DecimalFormat DF = new DecimalFormat("0.000");
	
	public void replay(StochasticLabelledPetriNetWithExogenousData net,
			ExogenousAnnotatedLog xlog) throws Exception {
		SLPNEDSemantics semantics = net.getDefaultSemantics();
		ExogenousDataStateLogAdapter adapter = new ExogenousDataStateLogAdapter(
				semantics, 
				xlog.getExogenousDatasets().toArray(new ExogenousDataset[0])
		);
		
		
		if (INTERESTED_CASE != null) {
			for(XTrace trace: xlog) {
				String concept = XUtils.getConceptName(trace);
				if (concept.contains(INTERESTED_CASE)){
					if (FIRING_SEQ != null) {
						replayTrace(adapter, semantics.clone(), trace, FIRING_SEQ);
					} else {
						replayTrace(adapter, semantics.clone(), trace);
					}
					
				}
				
			}
		} else {
			for(XTrace trace: xlog) {
					replayTrace(adapter, semantics.clone(), trace);
			}
		}
		
		
	}
	
	public void replayTrace(ExogenousDataStateLogAdapter adapter,
			SLPNEDSemantics semantics, XTrace trace) {
		replayMsg("START "+XUtils.getConceptName(trace));
		XEvent last = null;
		for(XEvent event : trace) {
			replayMsg(
					"Working on event with label :: "
					+ XUtils.getConceptName(event)
			);
			if (semantics.isFinalState()) {
				replayMsg("in final state but still "
						+ "had more events.");
				break;
			}
//			find a transition to fire
			String concept = XUtils.getConceptName(event);
			int transition = -1;
			int guess = -1;
			BitSet enabled = semantics.getEnabledTransitions();
			replayMsg("Looking for transition within enabled :: "
					+ enabled.toString()
			);
			for (int pos= enabled.nextSetBit(0);
					pos >= 0; pos = enabled.nextSetBit(pos+1)) {
				guess = pos; 
				if (concept.equals(semantics.getTransitionLabel(pos))) {
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
				debugState(semantics, transition, xstate, last, event, adapter);
				semantics.executeTransition(transition);
			}
			last = event;
		}
		if (!semantics.isFinalState()) {
			replayMsg("replay ended not in final state but "
					+ "had traversed all events.");
			ExogenousDataState xstate = (ExogenousDataState) adapter.fromEvent(trace.get(trace.size()-1));
			int transition = semantics.getEnabledTransitions().nextSetBit(0);
			debugState(semantics, transition, xstate, null, null, adapter);
		}
		replayMsg("END");
	}
	
	public void replayTrace(ExogenousDataStateLogAdapter adapter,
			SLPNEDSemantics semantics, XTrace trace,
			int[] firing) throws Exception {
		int fireId = 0;
		replayMsg("START "+XUtils.getConceptName(trace));
		XEvent last = null;
		for(XEvent event : trace) {
			if (fireId >= firing.length) {
				replayMsg("Ending firing but still had events lefts.");
				break;
			}
//			find a transition to fire
			int transition = firing[fireId];
			BitSet enabled = semantics.getEnabledTransitions();
			if (!enabled.get(transition)) {
				throw new Exception("[REPLAY] Could not fire transition :: "
						+transition
						+" in state :: "
						+semantics.toString());
			}
			DataState state = adapter.fromEvent(event);
			if (state instanceof ExogenousDataState) {
				ExogenousDataState xstate = (ExogenousDataState) state;
				debugState(semantics, transition, xstate, last, event, adapter);
				semantics.executeTransition(transition);
			}
			fireId += 1;
			last = event;
		}
		if (fireId < firing.length) {
			replayMsg("replay lacks events for all firings"
					+ " continuing with firing sequence.");
			ExogenousDataState xstate = (ExogenousDataState) 
					adapter.fromEvent(trace.get(trace.size()-1));
			while(fireId < firing.length) {
				int transition = firing[fireId];
				debugState(semantics, transition, xstate, null, null, adapter);
				semantics.executeTransition(transition);
				fireId += 1;
			}
		}
		if (!semantics.isFinalState()) {
			replayMsg("replay ended not in final state but "
					+ "had traversed all events.");
			ExogenousDataState xstate = (ExogenousDataState) 
					adapter.fromEvent(trace.get(trace.size()-1));
			int transition = semantics.getEnabledTransitions().nextSetBit(0);
			debugState(semantics, transition, xstate, null, null, adapter);
		}
		replayMsg("END");
	}
	
	public void debugState(SLPNEDSemantics semantics, int transition,
			ExogenousDataState xstate, 
			XEvent last,
			XEvent curr,
			ExogenousDataStateLogAdapter adapter) {
		debugMsg(
				" Firing transition :: "
				+semantics.getTransitionLabel(transition)
				+ " ("
				+ transition
				+")"
		);
//		show powers for this state
		debugMsg(
				" Current state :: "
				+ xstate.toFancyString()
		);
//		show choice data extracted, if an event is attached.
		choiceMsg(
				formatChoiceData(xstate, transition, semantics)
		);
		double[] powers = xstate.getPowers(semantics.getFactorTranslation());
		int[] knowns = xstate.getKnowns(semantics.getFactorTranslation());
//		breakdown weight for each enabled state
		BitSet enabled = semantics.getEnabledTransitions();
		for(int pos= enabled.nextSetBit(0); pos >= 0; 
				pos=enabled.nextSetBit(pos+1)) {
			String label = semantics.getTransitionLabel(pos);
//			check if we are interested in transition
			if (INTERESTED_TLABEL != null & label.equals(INTERESTED_TLABEL)) {
//				further, take 5 equal distance samples between last and curr
				if (last != null && curr != null) {
					long prevTime = ExogenousUtils.getEventTimeMillis(last);
					long currTime = ExogenousUtils.getEventTimeMillis(curr);
					if (Math.abs(prevTime - currTime) > 1) {
						double step = (currTime - prevTime) / 5.0f;
						XEvent dummyEv = (XEvent) curr.clone();
						while(prevTime <= currTime) {
							Date time = new Date(prevTime);
							((XAttributeTimestamp) 
								dummyEv.getAttributes().get("time:timestamp"))
								.setValue(time);
							ExogenousDataState newState = (ExogenousDataState) 
									adapter.fromEvent(dummyEv);
							double[] newPowers = newState.getPowers(
									semantics.getFactorTranslation());
							int[] newKnowns = newState.getKnowns(
									semantics.getFactorTranslation());
							interMsg(
									"transition "
									+ label
									+ "("
									+ pos
									+ ") could have been fired at "
									+ time
									+ " with a weight of "
									+ semantics.getTransitionWeight(pos, newPowers, newKnowns)
									+ " and a probability of "
									+ semantics.getTransitionWeight(pos, newPowers, newKnowns) / 
										semantics.getTotalWeightOfEnabledTransitions(newPowers, newKnowns)
								);
							prevTime += step;
						}
					}
				}
//				if so highlight
				interMsg(
						"Enabled transition, "
						+ label
						+ "("
						+ pos
						+ ") has a weight of "
						+ semantics.getTransitionWeight(pos, powers, knowns)
						+ " and a probability of "
						+ semantics.getTransitionWeight(pos, powers, knowns) / 
							semantics.getTotalWeightOfEnabledTransitions(powers, knowns)
					);
			} else {
				debugMsg(
						"Enabled transition, "
						+ label
						+ "("
						+ pos
						+ ") has a weight of "
						+ semantics.getTransitionWeight(pos, powers, knowns)
						+ " and a probability of "
						+ semantics.getTransitionWeight(pos, powers, knowns) / 
							semantics.getTotalWeightOfEnabledTransitions(powers, knowns)
					);
			}
			
		}
	}
	
	public String formatChoiceData(ExogenousDataState xstate, 
			int transition, SLPNEDSemantics semantics) {
		String ret = "";
//		format enabled
		BitSet enabled = semantics.getEnabledTransitions();
		ret += "$\\{ ";
		for(int pos=enabled.nextSetBit(0); pos >= 0; 
				pos=enabled.nextSetBit(pos+1)) {
			ret += semantics.getTransitionLabel(pos) +", ";
		}
		ret += "\\}$ & ";
//		format TTES
		ret += "$\\{ ";
		double[] powers = xstate.getPowers(semantics.getFactorTranslation());
		for(Entry<String, Integer> entry : 
			semantics.getFactorTranslation().entrySet()) {
			ret += entry.getKey() + " \\mapsto "
				   + DF.format(powers[entry.getValue()])
				   + ", ";
		}
		ret += "\\}$ & ";
//		format fired
		ret += "$[ "+semantics.getTransitionLabel(transition)+" ]$";
		return ret;
	}
	
//	logging functions
	protected static String DEBUG_CODE = "[DEBUG]";
	protected static String REPLAY_CODE = "[REPLAY]";
	protected static String INTER_CODE = "[!INTERESTED!]";
	protected static String CHOICE_CODE = "[~CHOICEDATA~]";
	public void replayMsg(String msg) {
		shoutMsg(REPLAY_CODE, msg);
	}
	public void debugMsg(String msg) {
		shoutMsg(DEBUG_CODE, msg);
	}
	public void interMsg(String msg) {
		shoutMsg(INTER_CODE, msg);
	}
	public void choiceMsg(String msg) {
		shoutMsg(CHOICE_CODE, msg);
	}
	public void shoutMsg(String code, String msg) {
		System.out.println(
				  code 
				+ " "
				+ msg
		);
	}

}
