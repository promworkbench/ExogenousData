package org.processmining.qut.exogenousdata.steps.linking;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XTrace;

import lombok.Builder;

@Builder
public class GrabFristLinker implements Linker{

	public List<XTrace> link(XTrace endogenous, List<XTrace> exoDataset) {
		// TODO Auto-generated method stub
		List<XTrace> ret = new ArrayList();
		if (exoDataset.size() > 0) {
			ret.add(exoDataset.get(0));
		}
		return ret;
	}

	public Boolean linkedTo(XTrace endogenous, XTrace exogenous) {
		return false;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "grabfirst";
	}

}
