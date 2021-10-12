package org.qut.exogenousaware;

import java.util.ArrayList;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.qut.exogenousaware.data.ExogenousAnnotatedLog;
import org.qut.exogenousaware.gui.ExogenousDiscoveryInvestigator;
import org.qut.exogenousaware.gui.ExogenousTraceExplorer;
import org.qut.exogenousaware.gui.ExogenousTraceView;



/**
 * Plugins to perform Exogenous-Aware Discovery using the framework presented in:
 * xPM: Process Mining with Exogenous Data
 * 
 * Current plugins are:
 * 	- Exogenous Aware Log Preperation
 * 	- Exogenous Trace Visualisation (Visualiser)
 *  - Exogenous Aware Discovery
 *  - Exogenous Discovery Investigator (Visualiser)
 * 	- Exogenous Aware Enhancement (not implemented)
*/
public class ExogenousAwareDiscoveryPlugin {
	
	
	
	
	
	@Plugin(
			name = "Exogenous Aware Log Preperation",
			parameterLabels = {"Process Log", "Exogenous Data Sources"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = "QUT",
			author = "A. Banham",
			email = "adam.banham@hdr.qut.edu.au"
	)
	public ExogenousAnnotatedLog preperation(UIPluginContext context, XLog endogenous, XLog[] exogenous) throws Throwable{
		ArrayList<XLog> exoLogs = new ArrayList<XLog>();
		for(XLog elog: exogenous) {
			exoLogs.add(elog);
		}
		ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
		
	}
	
	@Plugin(
			name = "Exogenous Trace Visualisation",
			parameterLabels = {"Endogenous Annotated Log"},
			returnLabels = {"ExogenousTraceExplorer"}, 
			returnTypes = {ExogenousTraceView.class}, 
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = "QUT",
			author = "A. Banham",
			email = "adam.banham@hdr.qut.edu.au"
	)
	@Visualizer
	public JComponent exogenousAnnotationViewing(UIPluginContext context, ExogenousAnnotatedLog xlog) throws Throwable{
		return new ExogenousTraceExplorer().visualise(
				context
				,
				ExogenousTraceView.builder()
				   .source(xlog)
				   .context(context)
				   .build()
				   .setup()
		);
	}
	
	@Plugin(name = "Exogenous Aware Discovery", parameterLabels = {"Endogenous Annotated Log","Control Flow DPN"}, returnLabels = {"Exogenous Discovery Investigator"}, returnTypes = {ExogenousDiscoveryInvestigator.class}, userAccessible = true)
	@UITopiaVariant(affiliation = "QUT", author = "A. Banham", email = "adam.banham@hdr.qut.edu.au")
	public ExogenousDiscoveryInvestigator exogenousDiscovery(UIPluginContext context, ExogenousAnnotatedLog exogenous, PetriNetWithData dpn) throws Throwable{
		
		ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
				.source(exogenous)
				.controlflow(dpn)
				.context(context)
				.build()
				.setup()
				.precompute();
		
		return edi;
	}
	
	@Plugin(
			name="Exogenous Discovery Investigator",
			level=PluginLevel.NightlyBuild,
			returnLabels= {"Exogenous Annotated Explorer UI"},
			returnTypes = { JComponent.class },
			userAccessible = true, 
			parameterLabels = { "" }
	)
	@Visualizer
	public JComponent exogenousDiscoveryViewing(UIPluginContext context, ExogenousDiscoveryInvestigator edi) {
		return edi;
	}
	
	@Plugin(name = "Exogenous Aware Enhancement", parameterLabels = {"Exogenous Annotated Log", "xDPN",}, returnLabels = {}, returnTypes = {}, userAccessible = true)
	@UITopiaVariant(affiliation = "QUT", author = "A. Banham", email = "adam.banham@hdr.qut.edu.au")
	public void exogenousEnhancement(UIPluginContext context, XLog exogenous, PetriNetWithData model) throws Throwable{
		System.out.println("hello world again again again!");
	}
}
