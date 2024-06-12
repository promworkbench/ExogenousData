package org.processmining.qut.exogenousaware.stochastic.conformance;

import java.util.HashMap;
import java.util.Map;

import org.processmining.qut.exogenousdata.data.ExogenousDataset;
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
			if (!known) {
				this.value = -1;
			} else {
				this.value = value;
			}
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
		
//		public int hashCode() {
//			return new HashCodeBuilder(17, 31)
//					.append(name)
//					.append(known)
//					.append(known ? value : -1)
//					.hashCode();
//		}
		
		
		
		public void reset() {
			this.known = false;
			this.value = 0.0;
		}
		
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (known ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			long temp;
			temp = Double.doubleToLongBits(value);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Factor other = (Factor) obj;
			if (known != other.known)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value))
				return false;
			return true;
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
	protected Map<String, Integer> datasets;
	int size;
	private int hash = -1;
	
	public ExogenousDataState(ExogenousDataset[] factors) {
		this.state = new HashMap();
		this.datasets = new HashMap();
		this.size = factors.length;
		for(int f=0; f < factors.length; f++) {
			state.put(f, new Factor(factors[f].getName()));
			datasets.put(factors[f].getName(), f);
		}
	}
	
	public ExogenousDataState(Map<Integer, Factor> state ) {
		this.state = new HashMap();
		this.datasets = new HashMap();
		this.size = state.keySet().size();
		for(int key : state.keySet()) {
			this.state.put(key, state.get(key).clone());
			this.datasets.put(state.get(key).name, key);
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
	
	public int[] getKnowns(Map<String,Integer> translation) {
		int[] knowns = new int[translation.keySet().size()];
		for(String name: translation.keySet()) {
			Factor factor = state.get(datasets.get(name));
			int idx = translation.get(name);
			knowns[idx] = factor.isKnown();
		}
		return knowns;
	}
	
	public double[] getPowers(Map<String,Integer> translation) {
		double[] powers = new double[translation.keySet().size()];
		for(String name: translation.keySet()) {
			Factor factor = state.get(datasets.get(name));
			int idx = translation.get(name);
			powers[idx] = factor.value;
		}
		return powers;
	}
	
	public Map<Integer, Factor> getState(){
		return this.state;
	}
	
	
	
//	@Override
//	public boolean equals(final Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj == null) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		final ExogenousDataState other = (ExogenousDataState) obj;
//		return this.hashCode() == other.hashCode();
//	}
	


	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExogenousDataState other = (ExogenousDataState) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
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
