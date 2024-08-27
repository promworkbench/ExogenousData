package org.processmining.qut.exogenousdata.stochastic.solver;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.Progress;

public class Solver {

	//other solvers:

	//https://scipopt.org/index.php#license

	public static final double lowest = 0.0001;
	
//	Controls for the parameter values
//	having a wider range is better than a small range for solver
	public static double MIN = 0.0001;
	public static double MAX = 100000.0;
	public static double GAP = (MAX - MIN) * 0.1;
	
//	how long do you want to optimise for
//	higher values could mean longer runtime
	public static int MAX_EVAL = 1000;
	public static int MAX_ITERS = 1000;
	public static int MAX_ATTEMPTS = MAX_EVAL * MAX_ITERS;
	
//	how carefully should the solver move
//	too big and the solution will default all factors to 1
//	too small and it will move impractically slow
	public static double LEARNING_RATE = 1e-10;
	
//	for feedback back to prom UI if set
	public static Progress PROG = null;
	public static UIPluginContext CONTEXT = null;

	/**
	 * 
	 * @param values
	 *            of the equations
	 * @param equations
	 * @param occurrences
	 *            of the equations
	 * @param numberOfParameters
	 * @param fixParameters
	 * @param initialParameterValues
	 * @return
	 */
	public static double[] solve(
			List<Equation> equations, 
			int numberOfParameters, 
			int[] fixParameters,
			int[] nonZeroParameters, 
			double[] initialParameterValues) {
		
		MultivariateJacobianFunction jfunction = new MultivariateJacobianFunction() {

			public Pair<RealVector, RealMatrix> value(RealVector point) {
				double[] pointD = point.toArray();

				RealVector value = new ArrayRealVector(equations.size());
				RealMatrix jacobian = new Array2DRowRealMatrix(equations.size(), numberOfParameters);

				for (int equationIndex = 0; equationIndex < equations.size(); equationIndex++) {
					Function observation = equations.get(equationIndex).getFunction();

					value.setEntry(equationIndex, observation.getValue(pointD));

					for (int parameterIndex = 0; parameterIndex < numberOfParameters; parameterIndex++) {
						double derivative = observation.getPartialDerivative(parameterIndex, pointD);
						if (Double.isInfinite(derivative) || Double.isNaN(derivative)) {
							double derv = observation.getPartialDerivative(parameterIndex, pointD);
							log("bad derivative :: "+derv);
//							System.out.println(observation.toString());
//							value.setEntry(equationIndex, lowest);
						}
						jacobian.setEntry(equationIndex, parameterIndex, derivative);
					}
				}
				return new Pair<RealVector, RealMatrix>(value, jacobian);
			}
		};

		final BitSet fixedParametersb = new BitSet();
		for (int i=0; i < fixParameters.length; i++) {
			if (fixParameters[i] == 1) {
				fixedParametersb.set(i);
			}
		}
		final BitSet nonZeroParametersb = new BitSet();
		for (int parameter : nonZeroParameters) {
			nonZeroParametersb.set(parameter);
		}
		setProgressorMax(MAX_ATTEMPTS);
		ParameterValidator validator = new ParameterValidator() {
			private int runs = 0;
			
			public RealVector validate(RealVector params) {
				runs += 1;
				log("["+runs+"] "
						+ "validating :: " + params);
				incrementProgressor();
				for (int i = 0; i < params.getDimension(); i++) {
					if (fixedParametersb.get(i)) {
						params.setEntry(i, initialParameterValues[i]);
						continue;
					}
					if (nonZeroParametersb.get(i) && params.getEntry(i) <= 0) {
						params.setEntry(i, MIN * 5);
					} 
					if (params.getEntry(i) < MIN) {
						params.setEntry(i, MIN * 5);
					} 
					if (params.getEntry(i) > MAX) {
						params.setEntry(i, MAX*0.98);
					}
				}
				return params;
			}
		};

		//set the initial guess
		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		for (int parameter = 0; parameter < numberOfParameters; parameter++) {
			initialGuess.setEntry(parameter, initialParameterValues[parameter]);
		}

		//		System.out.println("Initial guess: " + initialGuess);
		//		System.out.println("target " + Arrays.toString(values));
		//		RealVector initialGuess = new ArrayRealVector(numberOfParameters);
		//		initialGuess.set(2);

		//set the weights
		RealMatrix weight = new Array2DRowRealMatrix(
				equations.size(),
				equations.size()
		);
		for (int equation = 0; equation < equations.size(); equation++) {
			weight.setEntry(equation, equation, 
					equations.get(equation).getOccurrences()
			);
		}

		LeastSquaresProblem problem = new LeastSquaresBuilder()//
				.start(initialGuess)//
				.model(jfunction)//
				.target(Equation.getValues(equations))//
				.weight(weight)//
				.parameterValidator(validator)//
				.lazyEvaluation(false)//
				.maxEvaluations(MAX_EVAL)//
				.maxIterations(MAX_ITERS)//
				.build();
		LeastSquaresOptimizer optimiser = new LevenbergMarquardtOptimizer()
				.withCostRelativeTolerance(LEARNING_RATE)
				.withParameterRelativeTolerance(LEARNING_RATE);
//		LeastSquaresOptimizer optimiser = new GaussNewtonOptimizer(GaussNewtonOptimizer.Decomposition.CHOLESKY);
		Optimum optimum = optimiser.optimize(problem);

		//		System.out.println("RMS: " + optimum.getRMS());
		//		System.out.println("evaluations: " + optimum.getEvaluations());
		//		System.out.println("iterations: " + optimum.getIterations());
		double[] ret = optimum.getPoint().toArray();
		
		int fixed = 0;
		for(int i=0; i < ret.length; i++) {
			if (fixedParametersb.get(i)) {
				log("ret for fixed parameter was :"
					+ ret[i]
					+" but was meant to be :"
					+ initialParameterValues[i]
					);
				fixed+=1;
			}
		}
		log("number of fixed vars :"+fixed);
		
		return optimum.getPoint().toArray();
	}
	
//	UI Functions to keep progressor updated during solving
	public static void log(String message) {
		System.out.println("[SLPNED-Solver] "+message);
		if (CONTEXT != null) {
			CONTEXT.log(message);
		}
	}
	
	
	public static void setProgressor(Progress progressor) {
		PROG = progressor;
	}
	
	public static void setProgressorMax(int max) {
		if (PROG != null) {
			PROG.setMaximum( 
					PROG.getMaximum()
					+ max
			);
		}
	}
	
	public static void incrementProgressor() {
		incrementProgressor(1);
	}
	
	public static void incrementProgressor(int amount) {
		if (PROG != null) {
			while(amount > 0) {
				PROG.inc();
				amount -= 1;
			}
		}
	}
}
