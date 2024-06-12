package org.processmining.qut.exogenousdata.steps.transform.gui;

import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.steps.determination.Determination;
import org.processmining.qut.exogenousdata.steps.linking.Linker;
import org.processmining.qut.exogenousdata.steps.slicing.Slicer;
import org.processmining.qut.exogenousdata.steps.transform.type.Transformer;

public class DummyDetermination extends Determination {

	DummyDetermination(ExogenousDataset panel, Linker linker, Slicer slicer,
			Transformer transformer) {
		super(panel, linker, slicer, transformer);
	}
	
	@Override
	public String toString() {
		String name = "??" + " -> ";
		name = name + "??" + " -> ";
		name = name + "??" + " -> ";
		name = name + "?? (Apply to all Determinations)";
		return name;
	}

}
