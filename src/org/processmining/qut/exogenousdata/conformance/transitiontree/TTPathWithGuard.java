package org.processmining.qut.exogenousdata.conformance.transitiontree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TTPathWithGuard implements TransitionTreePath<TTFlowWithGuard> {
	
	private List<TTFlowWithGuard> path;

	public TTPathWithGuard(List<TTFlowWithGuard> path) {
		super();
		this.path = path;
	}
	
	public void append(TTFlowWithGuard flow) {
		path.add(flow);
	}
	
	public void swap(TTFlowWithGuard oldFlow, TTFlowWithGuard newFlow) {
		if (path.contains(oldFlow)){
			path.replaceAll(f -> { 
				if (f.equals(oldFlow)) { return newFlow;} 
				return f;
			});
		}
	}
	
	public Iterable<String> variant() {
		// TODO Auto-generated method stub
		return path.stream()
			.map(p -> p.label())
			.collect(Collectors.toList());
	}
	
	public int length() {
		return path.size();
	}
	
	public Iterable<TTFlowWithGuard> getSteps() {
		return Collections.unmodifiableList(path);
	}
	
	public int uniqueness() {
		int prime = 7;
		int ret = 1;
		for(TTFlowWithGuard flow : path) {
			ret = prime * ret + flow.label().hashCode();
			try {
				ret = prime * ret + flow.guard().cannonRepresentation().hashCode();
			} catch (NullPointerException e) {
//				expected fail case
			}
		}
		return ret;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TTPathWithGuard other = (TTPathWithGuard) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}



}
