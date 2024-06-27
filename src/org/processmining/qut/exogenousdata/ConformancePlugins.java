package org.processmining.qut.exogenousdata;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.util.HTMLToString;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.qut.exogenousdata.decisionmining.conformance.Determinism;

/**
 * Plugins for performing conformance checking <br>
 * <br>
 * Current plugins are:<br>
 * <ul>
 * <li> Determinism for models with guards [1,2] </li>
 * <li> Guard-Recall for models with guards [3] </li>
 * <li> Guard-Precision for models with guards [3] </li>
 *</ul>
 *<br>
 *[1] 	A. Banham, S. J. J. Leemans, M. T. Wynn, R. Andrews, xPM: A framework 
 *       for process mining with exogenous data, in: Process Mining Workshops - 
 *       ICPM 2021 International Workshops, volume 433 of Lecture Notes in 
 *       Business Information Processing, Springer, 2021, pp. 85-97.
 *[2]	xPM: Enhancing Exogenous Data Visibility. Adam Banham, Sander J.J. 
 *		 Leemans, Moe T. Wynn, Robert Andrews, Kevin B. Laupland, Lucy Shinners.
 *		 Artificial Intelligence in Medicine 2022 (Accepted as-of 24/09/2022).
 *[3]	Comparing Conformance Checking for Decision Mining: An Axiomatic Approach
 *		 IEEE Access 2024, Adam Banham, Arthur H. M. ter Hofstede, 
 *		 Sander J.J. Leemans, Felix Mannhardt, Robert Andrews, Moe T. Wynn
*/
public class ConformancePlugins {
	
	public static final String version = ExogenousDataPlugins.version;
		
	@Plugin(
			name = "Compute determinism of a DPN",
			parameterLabels = {"Petri net with data"},
			returnLabels = {"DeterminismMeasure"},
			returnTypes = {HTMLToString.class},
			help=" Computes the determinism of a Petri with guards, which may " +
					"be a Petri net with data.\r\n" + 
					" Determinism computes the portion of transitions that are "
					+ "in the postset of\r\n" + 
					" places with at least two outgoing arcs and have a "
					+ "non-trivial guard \r\n" + 
					" associated with them."
					+ version,
			categories={PluginCategory.ConformanceChecking},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = "QUT",
			author = "A. Banham",
			email = "adam.banham@hdr.qut.edu.au",
			pack = "ExogenousData"
	)
	public HTMLToString ComputeDeterminism(
			final UIPluginContext context,
			final PetriNetWithData dpn) {
		return new HTMLToString() {

			public String toHTMLString(boolean includeHTMLTags) {
				return "Determinism: " + 
						Determinism.ComputeDeterminism(dpn); 
			}
		};
	}

}
