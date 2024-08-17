package org.processmining.qut.exogenousdata.conformance.bookkeeping;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class GuardRecallAxiom3 {
	
	Path modelFullPath = Paths.get(
			"tests","src-test","resource", "test_dpn_full_grec.pnml");
	Path modelEasyPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_c.pnml");
	Path modelMidPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_b.pnml");
	Path modelHardPath = Paths.get(
			"tests","src-test","resource", "paper_example_dpn_a.pnml");
	Path logPath = Paths.get( 
			"tests","src-test","resource", "paper_example_log.xes");

	@Test
	public void testAxiom() {
		fail();
	}

}
