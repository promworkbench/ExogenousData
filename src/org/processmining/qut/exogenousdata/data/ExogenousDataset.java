package org.processmining.qut.exogenousdata.data;

import java.awt.Color;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.qut.exogenousdata.exceptions.CannotConvertException;
import org.processmining.qut.exogenousdata.exceptions.ExogenousAttributeNotFoundException;
import org.processmining.qut.exogenousdata.exceptions.LinkNotFoundException;
import org.processmining.qut.exogenousdata.gui.colours.ColourScheme;
import org.processmining.qut.exogenousdata.steps.linking.Linker;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
public class ExogenousDataset {

	@NonNull @Getter XLog source;

	@Default @Getter ExogenousDatasetLinkType linkType = null;
	@Default @Getter ExogenousDatasetType dataType = null;
	@Default @Getter @Setter Color colourBase = ColourScheme.green;
	@Default @Getter Linker linker = null;
	@Default private Boolean setupCompleted = false;
	
//	stats
	private double mean;
	@Default private boolean computedMean = false;
	private double std;
	@Default private boolean computedStd = false;



	public ExogenousDataset setup() throws CannotConvertException {
		if (ExogenousUtils.isExogenousLog(source)) {
			// attempt to determine data type for measurements in log
			try {
				dataType = ExogenousUtils.findDataType(source);
				System.out.println("[ExogenousDataset] "
						+ "data type set as :: "+dataType.toString());
			} catch (ExogenousAttributeNotFoundException e) {
				System.out.println("[ExogenousDataset] "
						+ "Unable to determine datatype of log :: "
						+this.source.getAttributes().get("concept:name").toString());
				throw new CannotConvertException(source, this);
			}
			// attempt to determine link type for data set
			try {
				linkType = ExogenousUtils.findLinkType(source);
				System.out.println("[ExogenousDataset] "
						+ "link type set as :: "+linkType.toString());
			} catch (ExogenousAttributeNotFoundException e) {
				System.out.println("[ExogenousDataset] "
						+ "Unable to determine datatype of log :: "
						+this.source.getAttributes().get("concept:name").toString());
				throw new CannotConvertException(source, this);
			}		
			// attempt to construct a linker for data set
			linker = ExogenousUtils.constructLinker(source, this);
			System.out.println("[ExogenousDataset] "
					+ "linker created as :: "+linker.getClass().getSimpleName());

		} else {
			throw new CannotConvertException(source, this);
		}
		this.setupCompleted = true;
		return this;
	}



	/**
	 * Checks for a link between a trace and this exogenous dataset.
	 * @param trace to check for linkage
	 * @return whether linkage was found.
	 */
	public boolean checkLink(XTrace trace) {
		return linker.link(trace, source).size() > 0;
	}


	/**
	 * Checks for a link between a trace and returns the links found.
	 * @param trace to use as link source
	 * @return a collection of links
	 */
	public List<XTrace> findLinkage(XTrace trace) throws LinkNotFoundException {
		//		check for link
				if (!checkLink(trace)) {
					throw new LinkNotFoundException();
				}
		//		otherwise, find links
		return linker.link(trace, source);
	}

	public String getName() {
		String name = "Exogenous Data Set";
		boolean fallback = false;
		//		attempt to find exogenous:name on trace
		try {
			XTrace trace = this.source.get(0);
			name = trace.getAttributes().get("exogenous:name").toString();
		} catch (Exception e) {
			fallback = true;
		}
		if (fallback) {
			if (this.source.getAttributes().containsKey("concept:name")) {
				name = this.source.getAttributes().get("concept:name").toString();
			}
		}
		return name;
	}
		
	public double getMean() throws Throwable {
		if (!computedMean) {
			if (dataType == ExogenousDatasetType.NUMERICAL) {
				double mean = 0;
				int total = 0;
				for( XTrace xseries : source) {
					for(XEvent ev : xseries) {
						Object val = ExogenousDatasetAttributes.extractExogenousValue(ev);
						if (val instanceof Integer) {
							mean+= (int) val;
						} else if (val instanceof Double) {
							mean+= (double) val;
						} else if (val instanceof String) {
							mean+= Double.parseDouble((String) val);
						} else {
							System.out.println("[ExogenousDataset]"
									+ "Opps couldnt find transform for: "
									+ val.getClass()
									+"|"
									+ val);
						}
						total += 1;
					}
					
				}
				this.mean = mean / total;
				this.computedMean = true;
				System.out.println("[ExogenousDataset]"
						+ "Dataset ("+getName()+") mean computed :: "+mean);
				return this.mean;
			} else {
				throw new Exception("[ExogenousDataset]"
						+ "Unable to compute mean on non-numerical datasets.");
			}
		} else {
			return this.mean;
		}
		
	}
	
	public double getStd() throws Throwable {
		if (!computedStd) {
			if (dataType == ExogenousDatasetType.NUMERICAL) {
				double mean = getMean();
				double std = 0;
				int total = 0;
				for( XTrace xseries : source) {
					for(XEvent ev : xseries) {
						Object val = ExogenousDatasetAttributes.extractExogenousValue(ev);
						double valer = 0.0;
						if (val instanceof Integer) {
							valer += (int) val;
						} else if (val instanceof Double) {
							valer+= (double) val;
						} else if (val instanceof String) {
							valer+= Double.parseDouble((String) val);
						} else {
							System.out.println("[ExogenousDataset]"
									+ "Opps couldnt find transform for: "
									+ val.getClass()
									+"|"
									+ val);
						}
						std += Math.pow(valer - mean, 2.0);
						total += 1;
					}
					
				}
				this.std = Math.sqrt(std / total);
				System.out.println("[ExogenousDataset]"
						+ "Dataset ("+getName()+") std computed :: "+this.std);
				this.computedStd = true;
				return this.std;
			} else {
				throw new Exception("[ExogenousDataset]"
						+ "Unable to compute mean on non-numerical datasets.");
			}
		} else {
			return this.std;
		}		
	}

	@Override
	public String toString() {
		return getName() + "(" + this.dataType.getLabel() + ")";
	}

}
