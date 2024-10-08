package org.processmining.qut.exogenousdata.gui.promlist;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.DefaultWedgeBuilder;

/**
 * A factory for making wedge builders on the fly for ProMLists
 * 
 * To trigger dynamic colour changes to wedges in ProMList use the following call sequence:<br>
 * <br>
 * 
 * ProMTraceList<XTrace> controller <br>
 * controller.setWedgeBuilder(WedgeBuilderFactory.???(??)) <br>
 * controller.updateUI();<br>
 *
 * 
 */
public class WedgeBuilderFactory {
	

	private WedgeBuilderFactory() {};
	
	public static DefaultWedgeBuilder createEventHighlight(int eventNumber) {
		return new EventHighlightWedger(eventNumber);
	}
	
	public static DefaultWedgeBuilder createTraceHighlight(int traceNumber) {
		return new TraceHighlightWedger(traceNumber);
	}
	
	public static DefaultWedgeBuilder createTraceHighlight(String concept) {
		return new TraceHighlightWedger(concept);
	}
	
}
