package org.processmining.qut.exogenousdata.gui.promlist;

import java.awt.Color;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.DefaultWedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.qut.exogenousdata.gui.promlist.ProMListComponents.ExoEvent;

public class EventHighlightWedger extends DefaultWedgeBuilder{
	
	private int highlight = -1;
	
	public EventHighlightWedger(int eventNumber) {
		this.highlight = eventNumber;
	}
	
	@Override
	public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
		ExoEvent neweventype = (ExoEvent) event;
		neweventype.setHighlight(neweventype.eventID == this.highlight);
		return event.getWedgeColor();
		
	}
	
	@Override
	public Integer assignWedgeGap(Trace<? extends Event> trace, Event event) {
		ExoEvent neweventype = (ExoEvent) event;
		if ((neweventype.eventID == this.highlight)||(neweventype.eventID==(this.highlight-1))) {
			return 10;
		}
		return null;
	}

}
