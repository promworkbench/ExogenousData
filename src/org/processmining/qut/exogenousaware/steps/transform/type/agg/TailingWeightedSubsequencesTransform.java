package org.processmining.qut.exogenousaware.steps.transform.type.agg;

import java.util.List;

import org.processmining.qut.exogenousaware.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousaware.steps.slicing.data.SubSeries.Scaling;
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
	@Default private double espilion = 0.01;

	public TransformedAttribute transform(SubSeries subtimeseries) {
		double agg = 1;
		List<Double> relativeTimes = subtimeseries.getXSeries(true, Scaling.min);
		for (int k=1; k <= subtimeseries.size(); k++) {
//			int bot = (subtimeseries.getSubEvents().size() - (k - 1));
			double bot = Math.abs(relativeTimes.get(k-1));
			bot =+ 1.0;
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
		agg = Math.log(espilion+agg);
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+"tailagg"+aggerator.getName(), agg);
	}

	public String getName() {
		return this.name;
	}

}
