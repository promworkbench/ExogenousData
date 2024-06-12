package org.processmining.qut.exogenousdata.steps.determination.configs;

import java.util.ArrayList;
import java.util.List;

import org.processmining.qut.exogenousdata.data.ExogenousDataset;
import org.processmining.qut.exogenousdata.data.ExogenousDatasetType;
import org.processmining.qut.exogenousdata.steps.determination.Determination;
import org.processmining.qut.exogenousdata.steps.slicing.FutureOutcomeSlicer;
import org.processmining.qut.exogenousdata.steps.slicing.TimeAwareSlicer;
import org.processmining.qut.exogenousdata.steps.transform.type.PossibleOutcomeTransformer;
import org.processmining.qut.exogenousdata.steps.transform.type.linear.SlopeTransformer;

public class AIIM2022 {

	static private List<Double> slicingRanges = new ArrayList<Double>() {{ 
		add(2.0);
		add(4.0);
		add(6.0);
		add(12.0);
	}};
	
	static private long HOUR = 1000 * 60 * 60;
	
	private AIIM2022() {}
	
	static public List<Determination> getConfiguration(List<ExogenousDataset> datasets) {
		List<Determination> configuration = new ArrayList();
		
		for(ExogenousDataset dataset: datasets) {
			if (dataset.getDataType() == ExogenousDatasetType.NUMERICAL) {
				for(double range : slicingRanges) {
					configuration.add(
							Determination.builder() 
							.panel(dataset)
							.linker(dataset.getLinker())
							.slicer( TimeAwareSlicer.builder().timePeriod((long) (range * HOUR)).build())
							.transformer( SlopeTransformer.builder().build())
							.build()
					);
				}
			} else {
				configuration.add( 
						Determination.builder()
						.panel(dataset)
						.linker(dataset.getLinker())
						.slicer(FutureOutcomeSlicer.builder().timePeriod(HOUR * 48).build())
						.transformer(PossibleOutcomeTransformer.builder().Outcome("SEPSIS INFECTION").build())
						.build()
				);
			}
		}
		return configuration;
	}
}
