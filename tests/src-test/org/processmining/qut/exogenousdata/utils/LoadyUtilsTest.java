package org.processmining.qut.exogenousdata.utils;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.deckfour.xes.model.XLog;
import org.junit.Test;

public class LoadyUtilsTest {
	
	public static Path logFile = Paths.get(
			"tests","src-test","resource", "roadfines", "roadfines_big_sampled.xes");
	
	@Test
	public void testReadingXes() {
		XLog log = LoadyUtils.loadLogFromFile(logFile.toFile());
		if (log == null) {
			fail("Failed to parse log.");
		}
	}

}
