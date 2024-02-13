package org.processmining.qut.exogenousaware.steps.transform.type.agg;

import org.processmining.qut.exogenousaware.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousaware.steps.transform.data.TransformedAttribute;
import org.processmining.qut.exogenousaware.steps.transform.type.Transformer;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;

/*
 * A transformer that calls another transformer on the growing sub-squences 
 * using a FIFO que and weights each call by the length of the sub-sequence.
 */
@Builder
public class TailingWeightedSubsequencesTransform implements Transformer {
	
	@NonNull private Transformer aggerator;
	@Default private String name = "twsub";

	public TransformedAttribute transform(SubSeries subtimeseries) {
		double agg = 0;
		for (int k=1; k <= subtimeseries.size(); k++) {
			int bot = (subtimeseries.getSubEvents().size() - (k - 1));
//			System.out.println("calling with bot="+bot);
			double w = 1.0 / bot;
			SubSeries sample = SubSeries.builder()
					.subEvents(subtimeseries.getSubEvents().subList(0, k))
					.abvSlicingName("dummy")
					.slicingName("dummy")
					.dataset(subtimeseries.getDataset())
					.source(subtimeseries.getSource())
					.datatype(subtimeseries.getDatatype())
					.comesFrom(subtimeseries.getComesFrom())
					.endogenous(subtimeseries.getEndogenous())
					.build();
			w = w * this.aggerator.transform(sample).getValue();
			agg += w;
		}
		agg = Math.log(agg);
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+"tailagg"+aggerator.getName(), agg);
	}

	public String getName() {
		return this.name;
	}

}
