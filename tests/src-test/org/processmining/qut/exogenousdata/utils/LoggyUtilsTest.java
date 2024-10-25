package org.processmining.qut.exogenousdata.utils;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.junit.Test;
import org.xeslite.common.XUtils;

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
	
	@Test 
	public void testDeterminsticSample() {
		XLog log = LoadyUtils.loadLogFromFile(logFile.toFile());
		int samples = 50;
		XLog slog = LoggyUtils.sampleFromLog(log, samples);
		XLog slog2 = LoggyUtils.sampleFromLog(log, samples);
		for(int i=0;i < slog.size(); i++) {
			XTrace t1 = slog.get(i);
			XTrace t2 = slog2.get(i);
			assertEquals(
					TraceyUtils.getControlFlowVariant(t1), 
					TraceyUtils.getControlFlowVariant(t2)
			);
			assertEquals(
					XUtils.getConceptName(t1),
					XUtils.getConceptName(t2)	
			);
		}
	}
	
	@Test 
	public void testDeterminsticLargerSample() {
		XLog log = LoadyUtils.loadLogFromFile(logFile.toFile());
		int samples = 50;
		XLog slog = LoggyUtils.sampleFromLog(log, samples);
		XLog slog2 = LoggyUtils.sampleFromLog(log, samples+50);
		for(int i=0;i < slog.size(); i++) {
			XTrace t1 = slog.get(i);
			XTrace t2 = slog2.get(i);
			assertEquals(
					TraceyUtils.getControlFlowVariant(t1), 
					TraceyUtils.getControlFlowVariant(t2)
			);
			assertEquals(
					XUtils.getConceptName(t1),
					XUtils.getConceptName(t2)	
			);
		}
	}
	
	@Test 
	public void testDeterminsticSampleFromSeperateLoadedLogs() {
		XLog log = LoadyUtils.loadLogFromFile(logFile.toFile());
		int samples = 50;
		XLog slog = LoggyUtils.sampleFromLog(log, samples);
		log = LoadyUtils.loadLogFromFile(logFile.toFile());
		XLog slog2 = LoggyUtils.sampleFromLog(log, samples);
		for(int i=0;i < slog.size(); i++) {
			XTrace t1 = slog.get(i);
			XTrace t2 = slog2.get(i);
			assertEquals(
					TraceyUtils.getControlFlowVariant(t1), 
					TraceyUtils.getControlFlowVariant(t2)
			);
			assertEquals(
					XUtils.getConceptName(t1),
					XUtils.getConceptName(t2)	
			);
		}
	}

}
