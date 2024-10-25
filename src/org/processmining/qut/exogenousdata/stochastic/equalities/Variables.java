package org.processmining.qut.exogenousdata.stochastic.equalities;

import org.processmining.basicstochasticminer.solver.Variable;
import org.processmining.basicstochasticminer.solver.VariablePower;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.qut.exogenousdata.stochastic.choicedata.ChoiceExogenousPoint;

public class Variables {

		private Variables() {};
		
		public static String getBaseWeightName(Transition trans) {
			String transName = trans.getId().toString().replace(" ", "_");
			return String.format(BASE_WEIGHT, transName);
		}
		
		public static String getExogenousAdjust(Transition trans, ChoiceExogenousPoint point) {
			String transName = trans.getId().toString().replace(" ", "_");
			return String.format(EXO_WEIGHT, transName, point.getName().replace(" ", "_"));
		}
		
		public static String getNotExogenousAdjust(Transition trans, ChoiceExogenousPoint point) {
			String transName = trans.getId().toString().replace(" ", "_");
			return String.format(NOT_EXO_WEIGHT, transName, point.getName().replace(" ", "_"));
		}
		
		public static enum SLPNEDVarType {
			BASE("BW_"),
			EXOADJ("AX_"),
			NOTEXOADJ("NAX_"),
			UNKNOWN("");
			
			public String startsWith;
			
			private SLPNEDVarType(String startsWith) {
				this.startsWith = startsWith;
			}
			
			public static SLPNEDVarType findType(String varName) {
				if (varName.startsWith(BASE.startsWith)) {
					return BASE;
				} else if (varName.startsWith(EXOADJ.startsWith)) {
					return EXOADJ;
				} else if (varName.startsWith(NOTEXOADJ.startsWith)) {
					return NOTEXOADJ;
				}
				return UNKNOWN;
			}
		}
		
		/**
		 * Minor wrapper to keep track of the transition that the variable relates to.
		 * @author Adam Banham
		 */
		public static class SLPNEDVariablePower extends VariablePower {
			
			protected Transition trans;
			protected SLPNEDVarType type;
			protected String dataset;
			protected int index;
			
			public SLPNEDVariablePower(int parameterIndex, String name, double power, Transition trans, String dname) {
				super(parameterIndex, name, power);
				this.index = parameterIndex;
				this.trans = trans;
				this.type = SLPNEDVarType.findType(name);
				if (dname != null) {
					this.dataset = dname;
				} else {
					this.dataset = "unsure";
				}
				
			}
			
			public int getIndex() {
				return index;
			}
			
			public Transition getTransition() {
				return this.trans;
			}
			
			public SLPNEDVarType getType() {
				return this.type;
			}
			
			public String getDataset() {
				return this.dataset;
			}
		}
		
		public static class SLPNEDVariable extends Variable {
			
			protected Transition trans;
			protected SLPNEDVarType type;
			protected String dataset;
			protected int index;

			public SLPNEDVariable(int parameterIndex, String name, Transition trans, String dname) {
				super(parameterIndex, name);
				this.index = parameterIndex;
				this.trans = trans;
				this.type = SLPNEDVarType.findType(name);
				if (dname != null) {
					this.dataset = dname;
				} else {
					this.dataset = "unsure";
				}
			}
			
			public int getIndex() {
				return index;
			}
			
			public Transition getTransition() {
				return this.trans;
			}
			
			public SLPNEDVarType getType() {
				return this.type;
			}
			
			public String getDataset() {
				return this.dataset;
			}
		}
		
//		String templates
		public static final String BASE_WEIGHT = "BW_%s";
		public static final String EXO_WEIGHT = "AX_%s_%s";
		public static final String NOT_EXO_WEIGHT = "NAX_%s_%s";
}
