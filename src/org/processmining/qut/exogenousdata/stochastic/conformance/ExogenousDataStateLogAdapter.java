package org.processmining.qut.exogenousdata.stochastic.conformance;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.exceptions.LinkNotFoundException;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceCollector.ChoiceCollectorParameters;
import org.processmining.qut.exogenousdata.stochastic.model.SLPNEDSemantics;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;
import org.processmining.stochasticlabelleddatapetrinet.logadapter.DataStateLogAdapter;

public class ExogenousDataStateLogAdapter implements DataStateLogAdapter {
	
	private SLPNEDSemantics semantics;
	private ExogenousDataset[] datasets;
	private double rounding = 0.05;
	private Scaling timeScaler = Scaling.monthly;
	private XAttributeMap traceAttrs;
	
	public ExogenousDataStateLogAdapter(
			SLPNEDSemantics semantics, 
			ExogenousDataset[] datasets) {
		this.semantics = semantics;
		this.datasets = datasets;
	}
	
	public ExogenousDataStateLogAdapter(
			SLPNEDSemantics semantics, 
			ExogenousDataset[] datasets,
			double rounding) {
		this.semantics = semantics;
		this.datasets = datasets;
		this.rounding = rounding;
	}
	
	
	public ExogenousDataStateLogAdapter(SLPNEDSemantics semantics, 
			ExogenousDataset[] datasets, double rounding,
			Scaling timeScaler) {
		super();
		this.semantics = semantics;
		this.datasets = datasets;
		this.rounding = rounding;
		this.timeScaler = timeScaler;
	}

	protected ExogenousDataStateLogAdapter clone() {
		return new ExogenousDataStateLogAdapter(
				this.semantics.clone(),
				this.datasets,
				this.rounding,
				this.timeScaler
		);
	}



	public DataState fromEvent(XEvent event) {
		ExogenousDataState state = new ExogenousDataState(datasets);
		ChoiceCollectorParameters ccp = ChoiceCollectorParameters.builder()
				.timeScaling(timeScaler)
				.rounding(rounding)
				.build();
		int varIdx = 0;
		for(ExogenousDataset data : datasets) {
			state.putDouble(varIdx, -1);
			try {
				double theta = ccp.computeEventTheta(event, traceAttrs, data);
				theta = theta - (theta % rounding) + rounding;
				state.putDouble(varIdx, theta);
//				state.putDouble(varIdx, 1);
			} catch (LinkNotFoundException e) {
				// expected outcome
				state.putDouble(varIdx, -1);
			} catch (Throwable e) {
				e.printStackTrace();
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
		return new ExogenousDataState(datasets);
	}

	public DataState fromTrace(XTrace trace, DataState ds) {
		traceAttrs = trace.getAttributes();
		return fromTrace(trace);
	}

}
