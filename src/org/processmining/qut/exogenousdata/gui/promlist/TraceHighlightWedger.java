package org.processmining.qut.exogenousdata.gui.promlist;

import java.awt.Color;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.DefaultWedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.qut.exogenousdata.gui.promlist.ProMListComponents.ExoEvent;
import org.processmining.qut.exogenousdata.gui.promlist.ProMListComponents.ExoTrace;
import org.xeslite.common.XUtils;

public class TraceHighlightWedger extends DefaultWedgeBuilder {
	
	private int highlight = -1;
	private String concept = null;
	
	public TraceHighlightWedger(int traceNumber) {
		this.highlight = traceNumber;
	}
	
	public TraceHighlightWedger(String concept) {
		this.concept = concept;
	}
	
	@Override
	public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
		ExoTrace newtracetype = (ExoTrace) trace;
		ExoEvent neweventype = (ExoEvent) event;
		if (concept != null) {
			neweventype.setHighlight(
					XUtils.getConceptName(newtracetype.source).equals(concept)
			);
		} else {
			neweventype.setHighlight(newtracetype.traceNo == this.highlight);
		}
		
		return event.getWedgeColor();
		
	}
}
