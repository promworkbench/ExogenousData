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

public class Solver {

	//other solvers:

	//https://scipopt.org/index.php#license

	public static final double lowest = 0.0001;

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
	public static double[] solve(List<Equation> equations, int numberOfParameters, int[] fixParameters,
			int[] nonZeroParameters, double[] initialParameterValues) {
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
							System.out.println("bad derivative :: "+derv);
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
		for (int parameter : fixParameters) {
			fixedParametersb.set(parameter);
		}
		final BitSet nonZeroParametersb = new BitSet();
		for (int parameter : nonZeroParameters) {
			nonZeroParametersb.set(parameter);
		}

		ParameterValidator validator = new ParameterValidator() {
			public RealVector validate(RealVector params) {
//								System.out.println("validate " + params);
				for (int i = 0; i < params.getDimension(); i++) {
					if (nonZeroParametersb.get(i) && params.getEntry(i) <= 0) {
						params.setEntry(i, lowest);
					} else if (fixedParametersb.get(i)) {
						params.setEntry(i, 1);
					} else if (params.getEntry(i) < lowest) {
						params.setEntry(i, lowest * 2);
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
		RealMatrix weight = new Array2DRowRealMatrix(equations.size(), equations.size());
		for (int equation = 0; equation < equations.size(); equation++) {
			weight.setEntry(equation, equation, equations.get(equation).getOccurrences());
		}

		LeastSquaresProblem problem = new LeastSquaresBuilder()//
				.start(initialGuess)//
				.model(jfunction)//
				.target(Equation.getValues(equations))//
				.weight(weight)//
				.parameterValidator(validator)//
				.lazyEvaluation(false)//
				.maxEvaluations(10000000)//
				.maxIterations(10000000)//
				.build();
		LeastSquaresOptimizer optimiser = new LevenbergMarquardtOptimizer().withCostRelativeTolerance(1.0e-10)
				.withParameterRelativeTolerance(1.0e-10);
//		LeastSquaresOptimizer optimiser = new GaussNewtonOptimizer(GaussNewtonOptimizer.Decomposition.CHOLESKY);
		Optimum optimum = optimiser.optimize(problem);

		//		System.out.println("RMS: " + optimum.getRMS());
		//		System.out.println("evaluations: " + optimum.getEvaluations());
		//		System.out.println("iterations: " + optimum.getIterations());

		return optimum.getPoint().toArray();
	}
}
