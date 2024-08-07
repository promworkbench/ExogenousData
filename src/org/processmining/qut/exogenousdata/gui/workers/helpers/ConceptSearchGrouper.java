package org.processmining.qut.exogenousdata.gui.workers.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousdata.utils.EventyUtils;
import org.processmining.qut.exogenousdata.utils.TraceyUtils;

/**
 * Groups based on the last seen event name.
 * 
 * @author n7176546
 *
 */
public class ConceptSearchGrouper implements ExogenousObserverGrouper {

	private Map<Integer,String> gidToName;
	private Map<String,Integer> nameToGid;
	
	public ConceptSearchGrouper(Set<String> concepts) {
		int group = 0;
		gidToName = new HashMap<Integer, String>();
		nameToGid = new HashMap<String, Integer>();
		for(String concept: concepts) {
			gidToName.put(new Integer(group), concept);
			nameToGid.put(concept, new Integer(group));
			group++;
		}
		System.out.println("[ConceptSearchGrouper] "
				+nameToGid);
		System.out.println("[ConceptSearchGrouper] "
				+gidToName);
	}
	
	public int findGroup(XTrace trace, SubSeries sliced) {
		// TODO Auto-generated method stub
		System.out.println("Checking :: "+trace);
		return nameToGid.get(
				EventyUtils.getConcept(trace.get(trace.size()-1))
		);
	}

	public int findGroup(XTrace trace, SubSeries sliced, int n) {
		// TODO Auto-generated method stub
		System.out.println("Checking :: "+TraceyUtils.getControlFlowVariant(trace)+"@"+n);
		int ret = nameToGid.get(
				EventyUtils.getConcept(trace.get(n))
		).intValue();
		System.out.println("returning :: "+ret);
		return ret;
	}

	public String getGroupName(int group) {
		// TODO Auto-generated method stub
		return gidToName.get(new Integer(group));
	}

}
