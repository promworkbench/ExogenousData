package org.processmining.qut.exogenousdata.steps.determination.configs;

import java.util.ArrayList;
import java.util.List;

import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.data.ExogenousDatasetType;
import org.processmining.qut.exogenousdata.steps.determination.Determination;
import org.processmining.qut.exogenousdata.steps.slicing.TimeAwareSlicer;
import org.processmining.qut.exogenousdata.steps.transform.type.linear.SlopeTransformer;
import org.processmining.qut.exogenousdata.steps.transform.type.modeller.PolynomialCurveFitterModeller;

public class ExemplarConfig {
	
	private ExemplarConfig() {};
	
	static private List<Double> slicingRanges = new ArrayList<Double>() {{ 
		add(10.0);
		add(5.0);
	}};
	
	static private long HOUR = 1000 * 60 * 60;
	
	static public List<Determination> getConfiguration(
			List<ExogenousDataset> datasets) {
		List<Determination> configuration = new ArrayList();
		
		for(ExogenousDataset dataset: datasets) {
			if (dataset.getDataType() == ExogenousDatasetType.NUMERICAL) {
				for(double range : slicingRanges) {
					configuration.add(
							Determination.builder() 
							.panel(dataset)
							.linker(dataset.getLinker())
							.slicer( TimeAwareSlicer.builder()
									.timePeriod((long) (range * HOUR)).build())
							.transformer( SlopeTransformer.builder().build())
							.build()
					);
				}
			}
			if (dataset.getName().contains("iron")) {
				configuration.add(
						Determination.builder() 
						.panel(dataset)
						.linker(dataset.getLinker())
						.slicer( TimeAwareSlicer.builder()
								.timePeriod((long) (25 * HOUR)).build())
						.transformer( SlopeTransformer.builder().build())
						.build()
				);
				configuration.add(
						Determination.builder() 
						.panel(dataset)
						.linker(dataset.getLinker())
						.slicer( TimeAwareSlicer.builder()
								.timePeriod((long) (25 * HOUR)).build())
						.transformer( PolynomialCurveFitterModeller.builder().build())
						.build()
				);
			}
		}
		return configuration;
	}

}
