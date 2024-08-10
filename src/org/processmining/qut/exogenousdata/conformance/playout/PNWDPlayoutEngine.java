package org.processmining.qut.exogenousdata.conformance.playout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.models.semantics.petrinet.impl.EfficientPetrinetSemanticsImpl;

public class PNWDPlayoutEngine 
	implements PlayoutEngine<PetriNetWithData, PlayoutTraceWithGuards> {
	
	private UIPluginContext context = null;
	
	public PNWDPlayoutEngine() {
		super();
	}

	public PNWDPlayoutEngine(UIPluginContext context) {
		super();
		this.context = context;
	}

	public Iterable<PlayoutTraceWithGuards> 
		generateTraces(PetriNetWithData model, int maxLength) {
//		construct playouts by firing transition from initial marking
		setProgCaption("Playing out the model...");
		setMaxForProg(maxLength);
		return new Iterable<PlayoutTraceWithGuards>() {
			
			public Iterator<PlayoutTraceWithGuards> iterator() {
				// TODO Auto-generated method stub
				List<PNWDFiringSequence> incomplete = new ArrayList();
				List<PNWDFiringSequence> done = new ArrayList();
//				start from the initial marking
				incomplete.add(new PNWDFiringSequence(
						model,
						new EfficientPetrinetSemanticsImpl(
								model,
								model.getInitialMarking()
						)
				));
				incMaxForProg();
				return new Iterator<PlayoutTraceWithGuards>() {
					
					public boolean hasNext() {
						// keep going until incomplete is empty
						return incomplete.size() > 0 || done.size() > 0;
					}

					public PlayoutTraceWithGuards next() {
						if (done.size() > 0) {
							return done.remove(0).convertToPlayout();
						}
						// pop out and process sequence
						setProgCaption("working on playoutset of "
								+ incomplete.size() 
								+ " (with length of "
								+ incomplete.get(0).visibleLength()
								+"/"
								+ maxLength
								+ ")...");
						done.clear();
						done.addAll(incomplete);
						incProgess();
						StopWatch watch = StopWatch.createStarted();
						System.out.println(
								"[PNWDPlayoutEngine] started batch..."
						);
						List<PNWDFiringSequence> next = new ArrayList();
						for(List<PNWDFiringSequence> group :incomplete.stream()
							.parallel()
							.map(i -> {
								List<PNWDFiringSequence> nexts = 
										new ArrayList<PNWDFiringSequence>();
								for(PNWDTransition trans : i.next()) {
									PNWDFiringSequence pot = i.fire(trans);
									if ( pot.visibleLength() <= maxLength) {
										nexts.add(pot);
									}
									
								}
								return nexts;
							}).collect(
								Collectors.toList()
							)){
							next.addAll(group);
						}
						watch.stop();
						System.out.println(
								"[PNWDPlayoutEngine] finished batch of "
								+ next.size()
								+ " after "
								+ watch.formatTime()
						);
//						System.out.println(next);
						incomplete.clear();
						incomplete.addAll(next);
						return done.remove(0).convertToPlayout();
					}
				};
			}
		};
	}
	
	private void setProgCaption(String caption) {
		if (context != null) {
			synchronized (context) {
				context.log(caption);
			}
			
		}
	}
	
	private void setMaxForProg(int max) {
		if (context != null) {
			synchronized (context) {
				context.getProgress().setMaximum( 
						context.getProgress().getMaximum() + max);
			}
		}
	}
	
	private void incMaxForProg() {
		if (context != null) {
			synchronized (context) {
				Progress progressor = context.getProgress();
				progressor.setMaximum( progressor.getMaximum() + 1);
			}
		}
	}
	
	private void incProgess() {
		if (context != null) {
			synchronized (context) {
				Progress progressor = context.getProgress();
//				context.log("current ::"
//						+ progressor.getValue()
//						+ "/"
//						+ progressor.getMaximum());
				progressor.setValue(progressor.getValue()+1);
				context.notify();
			}
		}
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
					semantics
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
			return new PlayoutTraceWithGuards(history, reachedFinal());
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
