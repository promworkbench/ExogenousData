package org.processmining.qut.exogenousaware.steps.transform.type.agg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	@Default private List<CachedVariance> cache = new ArrayList();
	
	private class CachedVariance {
		
		private int values;
		private double mean;
		private double std;

		public CachedVariance(int values, double mean, double std) {
			super();
			this.values = values;
			this.mean = mean;
			this.std = std;
		}

		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Objects.hash(mean, std, values);
			return result;
		}

		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CachedVariance other = (CachedVariance) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			return Double.doubleToLongBits(mean) == Double.doubleToLongBits(other.mean)
					&& Double.doubleToLongBits(std) == Double.doubleToLongBits(other.std) && values == other.values;
		}

		private TailingWeightedSubsequencesTransform getEnclosingInstance() {
			return TailingWeightedSubsequencesTransform.this;
		}
		
				
	}

	public TransformedAttribute transform(SubSeries subtimeseries) {
//		double agg = 1;
//		System.out.println("starting tailing weight ("+ subtimeseries.size()+").");
		List<Double> relativeTimes = subtimeseries.getXSeries(true, Scaling.min);
		List<Double> values = subtimeseries.getYSeries();
		double mean = subtimeseries.getComesFrom().getMean();
		double std = subtimeseries.getComesFrom().getStd();
		double agg = 0;
		if (std > 0) {
			double norm = 
					relativeTimes.stream()
						.mapToDouble(t -> 1.0/ (1.0 + Math.abs(t)))
						.reduce(0.0, Double::sum);
			agg = 
				IntStream.range(0, values.size()-1)
					.mapToDouble(i -> ( 1.0/ (1+Math.abs(relativeTimes.get(i))) * Math.abs((values.get(i) - mean)/ std)))
					.reduce(0.0, Double::sum);
			agg = agg / norm;
		}
			
		if (Double.isNaN(agg) || Double.isInfinite(agg)) {
			System.out.println("opps returning bad transform");
			agg = 0;
		}
		return new TransformedAttribute(subtimeseries.getAbvSlicingName()+"tailagg"+aggerator.getName(), agg);
	}
		
	public String getName() {
		return this.name;
	}

}
