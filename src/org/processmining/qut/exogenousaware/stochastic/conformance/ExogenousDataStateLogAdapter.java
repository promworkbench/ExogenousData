package org.processmining.qut.exogenousaware.stochastic.conformance;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.exceptions.LinkNotFoundException;
import org.processmining.qut.exogenousaware.stochastic.choicedata.ChoiceCollector.ChoiceCollectorParameters;
import org.processmining.qut.exogenousaware.stochastic.model.SLPNEDSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;

public class ExogenousDataStateLogAdapter implements DataStateLogAdapter {
	
	private SLPNEDSemantics semantics;
	private ExogenousDataset[] datasets;
	private XAttributeMap traceAttrs;
	
	public ExogenousDataStateLogAdapter(SLPNEDSemantics semantics, ExogenousDataset[] datasets) {
		this.semantics = semantics;
		this.datasets = datasets;
	}

	public DataState fromEvent(XEvent event) {
		ExogenousDataState state = new ExogenousDataState(datasets);
		ChoiceCollectorParameters ccp = ChoiceCollectorParameters.builder().build();
		int varIdx = 0;
		for(ExogenousDataset data : datasets) {
			try {
				state.putDouble(varIdx, ccp.computeEventTheta(event, traceAttrs, data));
			} catch (LinkNotFoundException e) {
				// TODO Auto-generated catch block
				state.putDouble(varIdx, -1);
			}
			varIdx++;
		}
//		System.out.println("made new ds from event :: "+state.toString());
		return state;
	}

	public DataState fromEvent(XEvent event, DataState ds) {
		return fromEvent(event);
	}

	public DataState fromTrace(XTrace trace) {
		traceAttrs = trace.getAttributes();
		return fromEvent(trace.get(0));
	}

	public DataState fromTrace(XTrace trace, DataState ds) {
		traceAttrs = trace.getAttributes();
		return fromTrace(trace);
	}

}
