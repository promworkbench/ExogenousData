package org.processmining.qut.exogenousdata;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.util.HTMLToString;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithDataFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.PetrinetWithMarkings;
import org.processmining.qut.exogenousdata.data.ExogenousAnnotatedLog;
import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.exceptions.CannotConvertException;
import org.processmining.qut.exogenousdata.gui.ExogenousDiscoveryInvestigator;
import org.processmining.qut.exogenousdata.gui.ExogenousTraceExplorer;
import org.processmining.qut.exogenousdata.gui.ExogenousTraceView;



/**
 * Plugins for process mining with exogenous data as presented in:<br>
 * <b>xPM: Process Mining with Exogenous Data [1]</b><br>
 * <br>
 * Current plugins are:<br>
 * <ul>
 * <li>Exogenous Aware Log Preperation [1,2]</li>
 * <li>Exogenous Trace Visualisation (Visualiser) [1,2]</li>
 * <li>Exogenous Aware Discovery [1,2]</li>
 * <li>Exogenous Discovery Investigator (Visualiser) [1,2]</li>
 * <li>Exogenous Aware Enhancement (EESA Visualisations and Ranking) [2]</li>
 *</ul>
 *<br>
 *[1] 	A. Banham, S. J. J. Leemans, M. T. Wynn, R. Andrews, xPM: A framework 
 *       for process mining with exogenous data, in: Process Mining Workshops - 
 *       ICPM 2021 International Workshops, volume 433 of Lecture Notes in 
 *       Business Information Processing, Springer, 2021, pp. 85-97.
 *[2]	xPM: Enhancing Exogenous Data Visibility. Adam Banham, Sander J.J. 
 *		 Leemans, Moe T. Wynn, Robert Andrews, Kevin B. Laupland, Lucy Shinners.
 *		 Artificial Intelligence in Medicine 2022 (Accepted as-of 24/09/2022).
*/
public class ExogenousDataPlugins {
	
	private static final String packageName = ExogenousDataStatics.packageName;
	private static final String version = ExogenousDataStatics.version;
	private static final String authora = ExogenousDataStatics.authora;
	private static final String authorEmail = ExogenousDataStatics.authoraEmail;
	private static final String authorAff = ExogenousDataStatics.authoraAff;
	
	
	@Plugin(
			name = "Exogenous Annotated Log Preparation (XLogs)",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
					+ " users to create determinations as identified by xPM [1]."
					+ " After building determinations, each one will be applied"
					+ " to all traces seen in the event log. Note that an xlog "
					+ "will be made but all changes will be done in place. This"
					+ " process is not memory efficient and may require systems "
					+ "to have more than 12 GB of heap available depending on the"
					+ " size of the exo-panels and event log. [1] xPM: Enhancing"
					+ " Exogenous Data Visibility. Adam Banham et. al. Artificial"
					+ " Intelligence in Medicine 2022 <br> See "
					+ " <a href=\"https://youtu.be/iSklEeNUJSc\" target=\"_blank\">"
					+ "https://youtu.be/iSklEeNUJSc</a> for a walkthough of tooling."
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousAnnotatedLog preperationWithXLogs(final UIPluginContext context, 
			final XLog endogenous, final XLog[] exogenous) throws Throwable{
		final List<ExogenousDataset> exoLogs = new ArrayList<ExogenousDataset>();
		for(final XLog elog: exogenous) {
			ExogenousDataset temp;
			try {
				temp = ExogenousDataset.builder()
						.source(elog)
						.build()
						.setup();
			} catch (final CannotConvertException e) {
				// if log cannot naively be convert to dataset then move on
				System.out.println(
					"[ExogenousAnnotatedLog] Cannot convert log='"
					+ elog.getAttributes().get("concept:name").toString()+"' "
					+ "to an exogenous dataset.");
				continue;
			}
			exoLogs.add(temp);
		}
		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(false)
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
	}
	
	@Plugin(
			name = "Exogenous Annotated Log Preparation",
			parameterLabels = {"Event Log", "Exo-Panels"},
			returnLabels = {"Exogenous Annotated Log"},
			returnTypes = {ExogenousAnnotatedLog.class},
			help="Given an event log and several exo-panels, this plugin allows"
					+ " users to create determinations as identified by xPM [1]."
					+ " After building determinations, each one will be applied"
					+ " to all traces seen in the event log. Note that an xlog "
					+ "will be made but all changes will be done in place. This"
					+ " process is not memory efficient and may require systems "
					+ "to have more than 12 GB of heap available depending on the"
					+ " size of the exo-panels and event log. [1] xPM: Enhancing"
					+ " Exogenous Data Visibility. Adam Banham et. al. Artificial"
					+ " Intelligence in Medicine 2022 <br> See "
					+ " <a href=\"https://youtu.be/iSklEeNUJSc\" target=\"_blank\">"
					+ "https://youtu.be/iSklEeNUJSc</a> for a walkthough of tooling."
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousAnnotatedLog preperation(final UIPluginContext context, 
			final XLog endogenous, final ExogenousDataset[] exogenous) throws Throwable{
		final List<ExogenousDataset> exoLogs = Arrays.asList(exogenous);

		final ExogenousAnnotatedLog annotated = ExogenousAnnotatedLog
				.builder()
				.endogenousLog(endogenous)
				.exogenousDatasets(exoLogs)
				.classifiers(endogenous.getClassifiers())
				.extensions(endogenous.getExtensions())
				.useDefaultConfiguration(false)
				.globalEventAttributes(endogenous.getGlobalEventAttributes())
				.globalTraceAttributes(endogenous.getGlobalTraceAttributes())
				.attributes(endogenous.getAttributes())
				.parsed(false)
				.build()
				.setup(context);
		return annotated;
	}
	
	
	@Plugin(
			name = "Exogenous Annotated Log Explorer",
			parameterLabels = {"Exogenous Annotated Log"},
			returnLabels = {"ExogenousTraceExplorer"}, 
			returnTypes = {ExogenousTraceView.class},
			help="This plugin allows users to explore an xlog through a GUI. "
					+ "Users can see slices that were related to events and "
					+ "see how the original exo-series evolved in comparision to"
					+ " the execution of the trace. See "
					+ " <a href=\"https://youtu.be/iSklEeNUJSc\" target=\"_blank\">"
					+ "https://youtu.be/iSklEeNUJSc</a> for a walkthough of tooling."
					+ version,
			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	@Visualizer
	public JComponent exogenousAnnotationViewing(final UIPluginContext context, 
			final ExogenousAnnotatedLog xlog) throws Throwable {
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
	
	@Plugin(
			name = "(Non) Exogenous Aware Discovery (DPN)",
			parameterLabels = {"Event log","Control Flow (DPN)"},
			returnLabels = {"Exogenous Discovery Investigator"},
			returnTypes = {ExogenousDiscoveryInvestigator.class},
			categories={PluginCategory.Analytics, PluginCategory.Enhancement,
						PluginCategory.Discovery
			},
			help="This plugin allows users to perform various process discovery "
					+ "methods using an event log (converted without exogenous data)"
					+ "and a control flow description. "
					+ " Such as performing decision mining and then exploring "
					+ "annotated transition guards using a visual format."
					+ "<br> See "
					+ " <a href=\"https://youtu.be/iSklEeNUJSc\" target=\"_blank\">"
					+ "https://youtu.be/iSklEeNUJSc</a> for a walkthough of tooling."
					+ version,
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDiscoveryInvestigator NonExogenousDiscovery_DPN(
			final UIPluginContext context, final XLog eventlog,
			final PetriNetWithData dpn) throws Throwable {
		
		final ExogenousAnnotatedLog xlog = ExogenousAnnotatedLog.builder()
				.endogenousLog(eventlog)
				.parsed(false)
				.showConfiguration(false)
				.build()
				.setup(context);
		
		final ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
				.source(xlog)
				.controlflow(dpn)
				.context(context)
				.build()
				.setup()
				.precompute();
		
		return edi;
	}
	
	public static final String ExogenousAwareDiscoveryHelp = ""
			+ "This plugin allows users to perform various process enhancement"
			+ " and discovery "
			+ "methods using an xlog and a control flow description. "
			+ " Such as performing decision mining and then exploring "
			+ "annotated transition guards using a visual format."
			+ "<br> See "
			+ " <a href=\"https://youtu.be/iSklEeNUJSc\" target=\"_blank\">"
			+ "https://youtu.be/iSklEeNUJSc</a> for a walkthough of tooling."
			+ version;
	
	@Plugin(
			name = "Exogenous Aware Discovery (DPN)",
			parameterLabels = {"Exogenous Annotated Log (xlog)","Control Flow (DPN)"},
			returnLabels = {"Exogenous Discovery Investigator"},
			returnTypes = {ExogenousDiscoveryInvestigator.class},
			categories={PluginCategory.Analytics, PluginCategory.Enhancement,
						PluginCategory.Discovery
			},
			help=ExogenousAwareDiscoveryHelp,
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDiscoveryInvestigator ExogenousDiscovery_DPN(
			final UIPluginContext context, final ExogenousAnnotatedLog exogenous,
			final PetriNetWithData dpn) throws Throwable {
		
		final ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
				.source(exogenous)
				.controlflow(dpn)
				.context(context)
				.build()
				.setup()
				.precompute();
		
		return edi;
	}
	
	@Plugin(
			name = "Exogenous Aware Discovery (PN)",
			parameterLabels = {"Exogenous Annotated Log (xlog)","Control Flow (PN)"},
			returnLabels = {"Exogenous Discovery Investigator"},
			returnTypes = {ExogenousDiscoveryInvestigator.class},
			categories={PluginCategory.Analytics, PluginCategory.Enhancement,
						PluginCategory.Discovery
			},
			help=ExogenousAwareDiscoveryHelp,
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDiscoveryInvestigator ExogenousDiscovery_PN(
			final UIPluginContext context, final ExogenousAnnotatedLog exogenous,
			final Petrinet pn) throws Throwable {
		
		final PetriNetWithDataFactory factory = new PetriNetWithDataFactory(pn, pn.getLabel());
		factory.cloneInitialAndFinalConnection(context);
		final PetriNetWithData dpn = factory.getRetValue();
		
		final ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
				.source(exogenous)
				.controlflow(dpn)
				.context(context)
				.build()
				.setup()
				.precompute();
		
		return edi;
	}
	
	@Plugin(
			name = "Exogenous Aware Discovery (PT)",
			parameterLabels = {"Exogenous Annotated Log (xlog)","Control Flow (PT)"},
			returnLabels = {"Exogenous Discovery Investigator"},
			returnTypes = {ExogenousDiscoveryInvestigator.class},
			categories={PluginCategory.Analytics, PluginCategory.Enhancement,
						PluginCategory.Discovery
			},
			help=ExogenousAwareDiscoveryHelp,
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDiscoveryInvestigator ExogenousDiscovery_PT(
			final UIPluginContext context, final ExogenousAnnotatedLog exogenous,
			final ProcessTree pt) throws Throwable {
		
		final PetrinetWithMarkings result = ProcessTree2Petrinet.convert(pt, true);
		
		final Petrinet pn = result.petrinet;
		
		context.addConnection(new InitialMarkingConnection(pn, result.initialMarking));
		context.addConnection(new FinalMarkingConnection(pn, result.finalMarking));
		
		final PetriNetWithDataFactory factory = new PetriNetWithDataFactory(pn, pn.getLabel());
		factory.cloneInitialAndFinalConnection(context);
		final PetriNetWithData dpn = factory.getRetValue();
		
		final ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
				.source(exogenous)
				.controlflow(dpn)
				.context(context)
				.build()
				.setup()
				.precompute();
		
		return edi;
	}
	
	@Plugin(
			name = "Exogenous Aware Discovery (APN)",
			parameterLabels = {"Exogenous Annotated Log (xlog)","Control Flow (APN)"},
			returnLabels = {"Exogenous Discovery Investigator"},
			returnTypes = {ExogenousDiscoveryInvestigator.class},
			categories={PluginCategory.Analytics, PluginCategory.Enhancement,
						PluginCategory.Discovery
			},
			help=ExogenousAwareDiscoveryHelp,
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDiscoveryInvestigator ExogenousDiscovery_APN(
			final UIPluginContext context, final ExogenousAnnotatedLog exogenous,
			final AcceptingPetriNet pn) throws Throwable {
		
		context.addConnection(new InitialMarkingConnection(pn.getNet(),
				pn.getInitialMarking()));
		for(Marking marking: pn.getFinalMarkings()) {
			context.addConnection(new FinalMarkingConnection(pn.getNet(),
					marking)
			);
		}
		
		
		final PetriNetWithDataFactory factory = 
				new PetriNetWithDataFactory(pn.getNet(), pn.getNet().getLabel());
		factory.cloneInitialAndFinalConnection(context);
		final PetriNetWithData dpn = factory.getRetValue();
		
		final ExogenousDiscoveryInvestigator edi = ExogenousDiscoveryInvestigator.builder()
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
	public JComponent exogenousDiscoveryViewing(final UIPluginContext context, 
			final ExogenousDiscoveryInvestigator edi) {
		return edi;
	}
	
//	@Plugin(
//			name = "Exogenous Aware Enhancement",
//			parameterLabels = {"Exogenous Annotated Log", "xDPN",},
//			categories={PluginCategory.Analytics, PluginCategory.Enhancement},
//			help="This plugin allows users to build and extract EESA visualisations."
//					+ " This plugin is currently under construction, and as such"
//					+ " is not fully implemented for use via a GUI."
//					+ version,
//			returnLabels = {}, returnTypes = {}, userAccessible = true
//	)
//	@UITopiaVariant(
//			affiliation = authorAff,
//			author = authora,
//			email = authorEmail,
//			pack = packageName
//	)
//	public void exogenousEnhancement(final UIPluginContext context, 
//			final XLog exogenous, final PetriNetWithData model) throws Throwable {
//		throw new NotImplementedException("Still under construction...");
//	}
	
	@Plugin(
			name = "Make Log into Exogenous Dataset",
			parameterLabels = {"Dataset Log",},
			categories={PluginCategory.Analytics},
			help="This plugin allows users to cast an event log into an "
					+ "exogneous dataset. "
					+ version,
			returnLabels = {"Exogenous Dataset"}, returnTypes = {ExogenousDataset.class}, 
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public ExogenousDataset createDataset(final UIPluginContext context, 
			final XLog datasetLog) throws Throwable {
		return ExogenousDataset.builder()
				.source(datasetLog)
				.build()
				.setup();
	}
	
	@Plugin(
			name = "Describe Exogenous Dataset",
			parameterLabels = {"Exogenous Dataset",},
			categories={PluginCategory.Analytics},
			help="This plugin allows users describe exogneous dataset."
					+ version,
			returnLabels = {"Description of ExogenousDataset"}, returnTypes = {HTMLToString.class}, 
			userAccessible = true
	)
	@UITopiaVariant(
			affiliation = authorAff,
			author = authora,
			email = authorEmail,
			pack = packageName
	)
	public HTMLToString describeDataset(final UIPluginContext context, 
			final ExogenousDataset dataset) throws Throwable {
		return new HTMLToString() {
			
			double mean = dataset.getMean();
			double std = dataset.getStd();

			public String toHTMLString(boolean includeHTMLTags) {
				
				Color colour = dataset.getColourBase();
				String hexcode = String.format("#%02X%02X%02X", 
						colour.getRed(), colour.getGreen(), colour.getBlue());  
				System.out.println(hexcode);
				
				return "<p>"
					   + "<b>Name:</b> " + dataset.getName() + "<br>"
					   + "<b>Size:</b> " + dataset.getSource().size() + "<br> "
					   + "<b>Preferred Colour:</b> " + colour.toString() 
					   		+ " ("+ hexcode + ") <br>"
					   + "<b>Dataset Type:</b> " + dataset.getDataType().toString() 
					   + "<br>"
					   + "<b>Data Mean:</b> " + mean + "<br>"
					   + "<b>Dataset Std:</b> " + std + "<br>"
					   + "</p>";
			}
		};
	}
}
