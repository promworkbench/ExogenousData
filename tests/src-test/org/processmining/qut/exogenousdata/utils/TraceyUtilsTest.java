package org.processmining.qut.exogenousdata.utils;

import static org.junit.Assert.assertEquals;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
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
	
	@Test
	public void testConstuctingDummyTrace() {
		String expected = "<A,B,C>";
		XTrace ret = TraceyUtils.createDummyVariant("A","B","C");
		String actual = TraceyUtils.getControlFlowVariant(ret);
		assertEquals(expected, actual);
	}

}
