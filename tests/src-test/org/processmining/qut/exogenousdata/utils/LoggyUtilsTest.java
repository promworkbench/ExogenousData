package org.processmining.qut.exogenousdata.utils;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.deckfour.xes.model.XLog;
import org.junit.Test;

public class LoggyUtilsTest {
	
	public static Path logFile = Paths.get(
			"tests","src-test","resource", "roadfines", "roadfines_big_sampled.xes");

	@Test
	public void testSample() {
		XLog log = LoggyUtils.sampleFromLog(
				LoadyUtils.loadLogFromFile(logFile.toFile()), 50);
		
		assertEquals(50, log.size());
	}
	
	@Test
	public void testSampleLarger() {
		XLog log = LoadyUtils.loadLogFromFile(logFile.toFile());
		int actualSize = log.size();
		log = LoggyUtils.sampleFromLog(log, actualSize+5);
		
		assertEquals(actualSize, log.size());
	}

}
