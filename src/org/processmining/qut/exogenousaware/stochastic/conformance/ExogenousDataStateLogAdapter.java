package org.processmining.qut.exogenousaware.stochastic.conformance;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.qut.exogenousaware.stochastic.model.SLPNEDSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;

public class ExogenousDataStateLogAdapter implements DataStateLogAdapter {
	
	private SLPNEDSemantics semantics;
	private ExogenousDataset[] datasets;
	
	public ExogenousDataStateLogAdapter(SLPNEDSemantics semantics, ExogenousDataset[] datasets) {
		this.semantics = semantics;
		this.datasets = datasets;
	}

	public DataState fromEvent(XEvent event) {
		DataState state = new ExogenousDataState(datasets);
		return state;
	}

	public DataState fromEvent(XEvent event, DataState ds) {
		return fromEvent(event);
	}

	public DataState fromTrace(XTrace trace) {
		return fromEvent(trace.get(0));
	}

	public DataState fromTrace(XTrace trace, DataState ds) {
		return fromTrace(trace);
	}

}
