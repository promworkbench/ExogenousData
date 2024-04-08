package org.processmining.qut.exogenousaware.gui.colours;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Builder.Default;

@Builder
public class ExoPanelPicker {

	@Default private List<Color> colours = new ArrayList<Color>(){{
		add(ColourScheme.red);
		add(ColourScheme.purple);
		add(ColourScheme.pink);
		add(ColourScheme.blue);
		add(ColourScheme.coral);
		add(ColourScheme.green);
		add(ColourScheme.yellow);
		add(ColourScheme.orange);
	}};
	@Default private int counter = 0;
	
	public Color getColour() {
		Color returner = colours.get(counter);
		counter++;
		if (counter >= colours.size() ) {
			counter = 0;
		}
		return returner;
	}
}
