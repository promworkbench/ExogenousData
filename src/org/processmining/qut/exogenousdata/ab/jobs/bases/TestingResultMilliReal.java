package org.processmining.qut.exogenousdata.ab.jobs.bases;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestingResultMilliReal implements TestingResult<Long, Double> {
	
	private Map<String, Long> timings;
	private Map<String, Double> measures;
	
	public TestingResultMilliReal() {
		this.timings = new HashMap();
		this.measures = new HashMap();
	}

	public Map<String, Long> getTimings() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableMap(this.timings);
	}

	public Map<String, Double> getMeasurements() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableMap(this.measures);
	}

	public void addTiming(String name, Long ms) {
		// TODO Auto-generated method stub
		this.timings.put(name, ms);
	}

	public void addMeasurement(String name, Double measure) {
		// TODO Auto-generated method stub
		this.measures.put(name, measure);
	}

	public String toString() {
		String ret = "";
		ret += "[Measures] { ";
		for(Entry<String, Double> e : this.measures.entrySet()) {
			ret += e.getKey()
				+"="
				+e.getValue()
				+", ";
		}
		ret += "} ";
		ret += "[Timing] { ";
		for(Entry<String, Long> e : this.timings.entrySet()) {
			ret += e.getKey()
				+"="
				+e.getValue()
				+" ms, ";
		}
		ret += "}";
		return ret;
	}
	
	

	
}
