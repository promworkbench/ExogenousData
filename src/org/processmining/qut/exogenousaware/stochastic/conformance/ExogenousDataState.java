package org.processmining.qut.exogenousaware.stochastic.conformance;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.processmining.qut.exogenousaware.data.ExogenousDataset;
import org.processmining.stochasticlabelleddatapetrinet.datastate.DataState;

public class ExogenousDataState implements DataState{
	
	public static class Factor {
		
		boolean known;
		String name;
		double value;
		
		public Factor(String name) {
			this.known = false;
			this.name = name;
			this.value = 0.0;
		}
		
		public Factor(String name, boolean known, double value) {
			this.name = name;
			this.known = known;
			this.value = value;
		}
		
		public void set(boolean known, double value) {
			this.known = known;
			this.value = value;
		}
		
		public int isKnown() {
			return known ? 1 : 0;
		}
		
		public double getValue() {
			return value;
		}
		
		public Factor clone() {
			return new Factor(name, known, value);
		}
		
		public int hashCode() {
			return new HashCodeBuilder(17, 31)
					.append(name)
					.append(known)
					.append(known ? value : -1)
					.hashCode();
		}
		
		public void reset() {
			this.known = false;
			this.value = 0.0;
		}
		
		public String toString() {
			String ret = "";
			if (known) {
				ret += Double.toString(value);
			} else {
				ret += "?";
			}
			return ret;
		}
	}
	
	protected Map<Integer, Factor> state;
	int size;
	private int hash = -1;
	
	public ExogenousDataState(ExogenousDataset[] factors) {
		this.state = new HashMap();
		this.size = factors.length;
		for(int f=0; f < factors.length; f++) {
			state.put(f, new Factor(factors[f].getName()));
		}
	}
	
	public ExogenousDataState(Map<Integer, Factor> state ) {
		this.state = new HashMap();
		this.size = state.keySet().size();
		for(int key : state.keySet()) {
			this.state.put(key, state.get(key).clone());
		}
	}
	
	public Factor findIndex(int varIdx) {
		return state.get(varIdx);
	}

	public void putDouble(int varIdx, double value) {
		Factor fact = findIndex(varIdx);
		fact.set(value > 0, value);		
	}

	public void putLong(int varIdx, long value) {
		// TODO Auto-generated method stub
		
	}

	public long getLong(int varIdx) {
		return 0;
	}

	public double getDouble(int varIdx) {
		Factor fact = findIndex(varIdx);
		if (varIdx % 2 == 0) {
			return fact.getValue();
		}
		return fact.isKnown();
	}

	public Double tryGetDouble(int varIdx) {
		Factor fact = findIndex(varIdx);
		if (varIdx % 2 == 0) {
			return fact.getValue();
		}
		return (double) fact.isKnown();
	}

	public Long tryGetLong(int varIdx) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean contains(int varIdx) {
		return true;
	}

	public boolean isEmpty() {
		return false;
	}

	public int capacity() {
		return size;
	}

	public int size() {
		return size;
	}

	public void clear() {
		for(int i =0; i < size()/2; i++) {
			Factor f = state.get(i);
			f.reset();
		}
	}

	public void update(DataState dataEffect) {
		if (dataEffect instanceof ExogenousDataState) {
			ExogenousDataState ds = (ExogenousDataState) dataEffect;
			Map<Integer, Factor> state = ds.getState();
			for( int key : state.keySet()) {
				this.state.put(key, state.get(key).clone());
			}
		}
	}

	public DataState makeStateImmutable() {
		hash = hashCode();
		return this;
	}

	public DataState deepCopy() {
		// TODO Auto-generated method stub
		return new ExogenousDataState(state);
	}
	
	public int hashCode() {
		if (hash > 0) {
			return hash;
		}
		HashCodeBuilder hasher = new HashCodeBuilder(17, 37);
		for(int i=0; i < (size/2); i++) {
			hasher.append(state.get(i).hashCode());
		}
		return hasher.hashCode();
	}
	
	public Map<Integer, Factor> getState(){
		return this.state;
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ExogenousDataState other = (ExogenousDataState) obj;
		return this.hashCode() == other.hashCode();
	}
	
	public String toString() {
		String ret = "(";
		for( int key=0; key < size(); key++) {
			ret += state.get(key).toString() +",";
		}
		ret = ret.substring(0, ret.length()-1);
		ret += ")";
		return ret;
	}

}
