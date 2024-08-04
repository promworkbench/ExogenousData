package org.processmining.qut.exogenousdata.utils;

import static org.junit.Assert.assertEquals;

import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.log.utils.XLogBuilder;

public class TraceyUtilsTest {

	@Test
	public void constructStringVariants() {
		XLog log = XLogBuilder.newInstance()
			.startLog("tester")
			.addTrace("dummy")
			.addEvent("A")
			.addEvent("B")
			.addEvent("C")
			.addTrace("dummy 2")
			.build();
		
		String ret = TraceyUtils.getControlFlowVariant(log.get(0));
		assertEquals("<A,B,C>", ret);
		ret = TraceyUtils.getControlFlowVariant(log.get(1));
		assertEquals("<>", ret);
	}

}
