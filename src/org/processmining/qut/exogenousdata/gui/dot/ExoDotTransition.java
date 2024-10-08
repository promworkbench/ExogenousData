package org.processmining.qut.exogenousdata.gui.dot;

import java.util.HashMap;

import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.qut.exogenousdata.data.dot.GuardExpressionHandler;


public class ExoDotTransition extends DotNode {

	private String oldLabel;
	private String controlFlowId;
	private String transLabel;
	private GuardExpressionHandler guard;
	private boolean highlighted = false;

	private String highlightLabel = "";
	
	public ExoDotTransition(String oldLabel, String controlFlowId, String transLabel, GuardExpressionHandler guard) {
		super(oldLabel, 
				new HashMap<String,String>(){{
					put("style", "filled");
					put("fontcolor", "black");
					put("fillcolor", "none");
					put("shape", "none");
					put("width" , "1");
					put("height" , "1");
					put("margin", "0");
					put("border", "0");
				}}
		
		);
		setOption("layer", "net");
		this.oldLabel = oldLabel;
		this.transLabel = transLabel;
		this.controlFlowId = controlFlowId;
		this.highlightLabel = this.makeHighlightLabel(oldLabel);
		this.guard = guard;
	}
	
	public GuardExpressionHandler getGuardExpression() {
		return this.guard;
	}
	
	public String getControlFlowId() {
		return this.controlFlowId;
	}
	
	public void setControlFlowId(String id) {
		this.controlFlowId = id;
	}
	
	public String getTransLabel() {
		return this.transLabel;
	}
	
	public void setTransLabel(String label) {
		String regex = "<B>.*</B>";
		String replace = "<B>"
				+label
				+"</B>";
		this.highlightLabel = this.highlightLabel.replaceFirst(regex, replace);
		this.oldLabel = this.oldLabel.replaceFirst(regex, replace);
		this.transLabel = label;
	}
	
	public String makeHighlightLabel(String label) {
		return label.replaceFirst("BGCOLOR=\".{1,10}\".PORT=\"TITLE\"","BGCOLOR=\"YELLOW\" PORT=\"TITLE\"");
	}
	
	public void highlightNode() {
		this.highlighted = true;
		this.setLabel(this.highlightLabel);
	}
	
	public void revertHighlight() {
		this.highlighted = false;
		this.setLabel(this.oldLabel);
	}
	
	public boolean isHighlighted() {
		return this.highlighted;
	}
	
	@Override
	public String toString() {
		String result = "\"" + getId() + "\" [label=" + labelToString() + ", id=\"" + getId() + "\"";
		for (String key : getOptionKeySet()) {
			result += "," + key + "=" + escapeString(getOption(key));
		}
		return result + "];";
	}
}
