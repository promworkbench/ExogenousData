package org.processmining.qut.exogenousdata.ab.jobs.bases;

import org.deckfour.xes.model.XLog;

public interface Configuration<T,J,K,L> {
	
	public abstract XLog trainingLog();
	public abstract XLog testingLog();
	public abstract T controlflow();
	public abstract J result();
	public abstract void setResult(J result);

	public abstract Object other(String name);
	public abstract void addOther(String name, Object other);
	
	public abstract TestingResult<K, L> makeTesting();
	public abstract void setResult(TestingResult<K, L> result);
	public abstract TestingResult<K, L> getResult();

}
