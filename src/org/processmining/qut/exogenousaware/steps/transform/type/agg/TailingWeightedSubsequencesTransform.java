package org.processmining.qut.exogenousaware.steps.transform.type.agg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
//		double agg = 1;
//		System.out.println("starting tailing weight ("+ subtimeseries.size()+").");
		List<Double> relativeTimes = subtimeseries.getXSeries(true, Scaling.min);
		List<Double> variances = transformToVariance(
				subtimeseries.getYSeries(),
				subtimeseries.getComesFrom().getMean(), 
				subtimeseries.getComesFrom().getStd());
		
		double agg = IntStream.
			rangeClosed(0,subtimeseries.size()-1)
			.parallel()
			.mapToDouble( (i) -> {
				return (1/(1+Math.abs(relativeTimes.get(i)))) * variances.get(i);
			})
			.reduce(1.0, (ls,nx) -> ls + nx);
			
//		for (int k=1; k <= subtimeseries.size(); k++) {
////			int bot = (subtimeseries.getSubEvents().size() - (k - 1));
//			double bot = Math.abs(relativeTimes.get(k-1));
//			bot =+ 1.0;
////			System.out.println("calling with bot="+bot);
//			double w = 1.0 / bot;
//			SubSeries sample = SubSeries.builder()
//					.subEvents(subtimeseries.getSubEvents().subList(0, k))
//					.abvSlicingName("dummy")
//					.slicingName("dummy")
//					.dataset(subtimeseries.getDataset())
//					.source(subtimeseries.getSource())
//					.datatype(subtimeseries.getDatatype())
//					.comesFrom(subtimeseries.getComesFrom())
//					.endogenous(subtimeseries.getEndogenous())
//					.build();
//			w = w * this.aggerator.transform(sample).getValue();
//			agg += w;
//		}
		agg = Math.log(espilion+agg);
		System.out.println("finished tailing weight ("+agg+").");
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+"tailagg"+aggerator.getName(), agg);
	}
	
	private List<Double> transformToVariance(List<Double> values, double mean, double std){
		
		List<Double> ret = new ArrayList();
		double curr = 0;
		for(double val : values) {
			if (std == 0) {
				ret.add(0.0);
				continue;
			}
			double now = (Math.abs(val - mean) / std);
			ret.add(now);
			curr += now;
		}
		return ret;
	}
		
	public String getName() {
		return this.name;
	}

}
