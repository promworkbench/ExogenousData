package org.processmining.qut.exogenousdata.conformance.playout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;

public class PNWDPlayoutEngine 
	implements PlayoutEngine<PetriNetWithData, PlayoutTraceWithGuards> {

	public Iterable<PlayoutTraceWithGuards> 
		generateTraces(PetriNetWithData model, int maxLength) {
//		construct playouts by firing transition from initial marking
		return new Iterable<PlayoutTraceWithGuards>() {
			
			public Iterator<PlayoutTraceWithGuards> iterator() {
				// TODO Auto-generated method stub
				List<PNWDFiringSequence> incomplete = new ArrayList();
				Set<PNWDFiringSequence> seen = new HashSet();
				List<PNWDFiringSequence> complete = new ArrayList();
//				start from the initial marking
				incomplete.add(new PNWDFiringSequence(
						model,
						new EfficientPetrinetSemanticsImpl(
								model,
								model.getInitialMarking()
						)
				));
				return new Iterator<PlayoutTraceWithGuards>() {
					
					public boolean hasNext() {
						// keep going until incomplete is empty
						return incomplete.size() > 0;
					}

					public PlayoutTraceWithGuards next() {
						// pop out and process sequence
						PNWDFiringSequence seq = incomplete.remove(0);
						seen.add(seq);
						for(PNWDTransition trans : seq.next()) {
							PNWDFiringSequence pot = seq.fire(trans);
							if (!seen.contains(pot) 
								&& pot.visibleLength() <= maxLength) {
								incomplete.add(pot);
							}
						}
						return seq.convertToPlayout();
					}
		
				};
			}
		};
	}
	
	public static class PNWDFiringSequence {
		
		private PetriNetWithData net;
		private List<PNWDTransition> history;
		private EfficientPetrinetSemanticsImpl semantics;
		
		public PNWDFiringSequence(PetriNetWithData net, 
				List<PNWDTransition> history,
				EfficientPetrinetSemanticsImpl semantics) {
			super();
			this.net = net;
			this.history = history;
			this.semantics = semantics;
		}

		public PNWDFiringSequence(PetriNetWithData net, 
				EfficientPetrinetSemanticsImpl semantics) {
			super();
			this.net = net;
			this.semantics = semantics;
			this.history = new ArrayList();
		}

		public Set<PNWDTransition> next(){
			Set<PNWDTransition> ret = new HashSet();
			Map<PNWDTransition, Integer> prevSilence = new HashMap<>();
//			go through history and count all connected silence firings 
//			since the last fired
			for(int i=history.size()-1; i >= 0; i--) {
				PNWDTransition el = history.get(i);
				if (el.isInvisible()) {
					int val = 0;
					if (prevSilence.containsKey(el)) {
						val = prevSilence.get(el);
					}
					prevSilence.put(el, val+1);
				} else {
					break;
				}
			}
//			work through choices, prune silent actions that have been
//			fired more than once in the history
			for(Transition trans: semantics.getExecutableTransitions()) {
				if (trans instanceof PNWDTransition) {
					PNWDTransition el = (PNWDTransition) trans;
					if (prevSilence.containsKey(el)) {
						if (prevSilence.get(el) <= 1) {
							ret.add(el);
						}
					} else {
						ret.add(el);
					}
					
				}
			}
			return ret;
		}
		
		public PNWDFiringSequence fire(PNWDTransition trans) {
			List<PNWDTransition> nhist = new ArrayList(history.size()+1);
			nhist.addAll(history);
			nhist.add(trans);
			EfficientPetrinetSemanticsImpl nsemantics = new EfficientPetrinetSemanticsImpl(
					net,
					semantics.getCurrentState()
			);
			nsemantics.directExecuteExecutableTransition(trans);
			return new PNWDFiringSequence(net, nhist, nsemantics);
		}
		
		public boolean reachedFinal() {
			boolean ret = false;
			Marking[] markings = net.getFinalMarkings();
			for(int i=0; i < markings.length; i++) {
				if (markings[i].equals(semantics.getCurrentState())) {
					ret = true;
					break;
				}
			}
			return ret;
		}
		
		public PlayoutTraceWithGuards convertToPlayout() {
			return new PlayoutTraceWithGuards(history);
		}
		
		public int visibleLength() {
			return (int) history.stream()
					.filter( t -> !t.isInvisible())
					.count();
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((history == null) ? 0 : history.hashCode());
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PNWDFiringSequence other = (PNWDFiringSequence) obj;
			if (history == null) {
				if (other.history != null)
					return false;
			} else if (!history.equals(other.history))
				return false;
			return true;
		}
		
		
		
	}

}
