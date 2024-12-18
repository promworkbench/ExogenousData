package org.processmining.qut.exogenousdata.steps.transform.type.agg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries;
import org.processmining.qut.exogenousdata.steps.slicing.data.SubSeries.Scaling;
import org.processmining.qut.exogenousdata.steps.transform.data.TransformedAttribute;
import org.processmining.qut.exogenousdata.steps.transform.type.Transformer;

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
	@Default private Scaling timeScaler = Scaling.day;
	
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
		if (subtimeseries == null) {
			System.out.println("subseries was null?");
		} else if (subtimeseries.size() < 1) {
			System.out.println("subseries was empty?");
		}
//		double agg = 1;
//		System.out.println("[TailingWeightedSubsequencesTransform] "
//				+ "starting tailing weight ("+ subtimeseries.size()+").");
		List<Double> relativeTimes = new ArrayList<Double>() {{
			for (long rel : subtimeseries.getXSeries(true)) {
				add(timeScaler.scale(rel));
			}
		}};
		List<Double> values = subtimeseries.getYSeries();
		
		double mean; 
		double std;
		double agg = 0;
		try {
			mean = subtimeseries.getComesFrom().getMean();
			std = subtimeseries.getComesFrom().getStd();
		} catch (Throwable e) {
//			mean didnt work out
			return new TransformedAttribute(
					subtimeseries.getAbvSlicingName()
					+"tailagg"+aggerator.getName(), agg);
		}
		if (std > 0) {
			double norm = 
					relativeTimes.stream()
						.mapToDouble(t -> 1.0/ (1.0 + Math.abs(t)))
						.reduce(0.0, Double::sum);
			if (norm == 0.0) {
				System.out.println("[TailingWeightedSubsequencesTransform] "
						+ "Computed norm is zero, transform is unlikely to work");
			}
			agg = 
				IntStream.range(0, values.size()-1)
					.mapToDouble(i -> 
					( 1.0 / ( 1+Math.abs(relativeTimes.get(i)))) 
				    * 
				    (Math.abs((values.get(i) - mean)/ std))
					)
					.reduce(0.0, Double::sum);
			agg = agg / norm;
		}
			
		if (Double.isNaN(agg) || Double.isInfinite(agg)) {
			System.out.println("[TailingWeightedSubsequencesTransform] "
					+ "opps returning bad transform ::"+agg);
			agg = 0;
		}
		return new TransformedAttribute(
				subtimeseries.getAbvSlicingName()
				+"tailagg"+aggerator.getName(), agg);
	}
		
	public String getName() {
		return this.name;
	}

}
