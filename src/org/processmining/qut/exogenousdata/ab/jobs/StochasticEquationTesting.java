package org.processmining.qut.exogenousdata.ab.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.basicstochasticminer.solver.Division;
import org.processmining.basicstochasticminer.solver.Equation;
import org.processmining.basicstochasticminer.solver.Function;
import org.processmining.basicstochasticminer.solver.Product;
import org.processmining.basicstochasticminer.solver.Solver;
import org.processmining.basicstochasticminer.solver.Sum;
import org.processmining.basicstochasticminer.solver.Variable;
import org.processmining.basicstochasticminer.solver.VariablePower;

public class StochasticEquationTesting {
	
	
	private static int RUNS = 100;
	private static int TYPE = 3;
	private static boolean SHOW_EQS = false;
	
	/*
	 * Main entry point
	 */
	public static void main(String[] args) throws Throwable {
		System.out.println("!--- Starting run 1 ---!");
		Tuple<List<Function>, double[]> out = runSolution();
//		testing for determinism
		for(int j = 0; j < RUNS-1; j++) {
			System.out.println(String.format("!--- starting run %d ---!", j+1));
			Tuple<List<Function>, double[]> inner = runSolution();
			for(int i = 0; i < inner.getRight().length; i++) {
				out.getRight()[i] += inner.getRight()[i];
			}
		}
		System.out.println(
				String.format("!--- average weight after %d runs ---!", RUNS)
		);
		for (int i = 0; i < out.getLeft().size(); i++) {
			System.out.println(
					"~~~ solved variable ~~~\n"+
					out.getLeft().get(i).toString()+
					" was set to "+
					out.getRight()[i] / RUNS
			);
		}
	}
	
	public static Tuple<List<Function>,double[]> runSolution() throws Exception {
		System.out.println("--- starting discovery of weights ---");
		Tuple<List<Equation>,List<Function>> out = buildEquations();
		System.out.println("--- built equations ---");
		int[] fixed = new int[out.getRight().size()];
		int[] nonzero = new int[out.getRight().size()];
		double[] inital = new double[out.getRight().size()];
		for(int i = 0; i < out.getRight().size(); i++) {
			fixed[i] = 0;
			inital[i] =  1;			
			nonzero[i] = 1;
		}
		double[] solvedvalues = Solver.solve(out.getLeft(), out.getRight().size(), fixed, nonzero, inital);
		System.out.println("--- found solution ---");
		return new Tuple<>(out.getRight(), solvedvalues);		
	}
	
	
	
	public static class VariableCounter {
		
		int count = 0;
		
		public VariableCounter() {
		}
		
		public int get() {
			return this.count;
		}
		
		public void incr() {
			this.count++;
		}
	}
	
	public static String BASE_T1 = "base_t_1";
	public static String BASE_T2 = "base_t_2";
	public static String BASE_T3 = "base_t_3";
	public static String BASE_T4 = "base_t_4";
	public static String BASE_T5 = "base_t_5";
	
	public static String ADJUST_X_T1 = "adjust_x_t_1";
	public static String ADJUST_X_T2 = "adjust_x_t_2";
	public static String ADJUST_X_T3 = "adjust_x_t_3";
	public static String ADJUST_X_T4 = "adjust_x_t_4";
	public static String ADJUST_X_T5 = "adjust_x_t_5";
	
	public static String NADJUST_X_T1 = "not_adjust_x_t_1";
	public static String NADJUST_X_T2 = "not_adjust_x_t_2";
	public static String NADJUST_X_T3 = "not_adjust_x_t_3";
	public static String NADJUST_X_T4 = "not_adjust_x_t_4";
	public static String NADJUST_X_T5 = "not_adjust_x_t_5";
	
	public static String ADJUST_Y_T1 = "adjust_y_t_1";
	public static String ADJUST_Y_T2 = "adjust_y_t_2";
	public static String ADJUST_Y_T3 = "adjust_y_t_3";
	public static String ADJUST_Y_T4 = "adjust_y_t_4";
	public static String ADJUST_Y_T5 = "adjust_y_t_5";
	
	public static String NADJUST_Y_T1 = "not_adjust_y_t_1";
	public static String NADJUST_Y_T2 = "not_adjust_y_t_2";
	public static String NADJUST_Y_T3 = "not_adjust_y_t_3";
	public static String NADJUST_Y_T4 = "not_adjust_y_t_4";
	public static String NADJUST_Y_T5 = "not_adjust_y_t_5";
	
	public static String ADJUST_Z_T1 = "adjust_z_t_1";
	public static String ADJUST_Z_T2 = "adjust_z_t_2";
	public static String ADJUST_Z_T3 = "adjust_z_t_3";
	public static String ADJUST_Z_T4 = "adjust_z_t_4";
	public static String ADJUST_Z_T5 = "adjust_z_t_5";
	
	public static String NADJUST_Z_T1 = "not_adjust_z_t_1";
	public static String NADJUST_Z_T2 = "not_adjust_z_t_2";
	public static String NADJUST_Z_T3 = "not_adjust_z_t_3";
	public static String NADJUST_Z_T4 = "not_adjust_z_t_4";
	public static String NADJUST_Z_T5 = "not_adjust_z_t_5";
	
	public static Tuple<String,String> T1_OPT = 
			new Tuple<>(ADJUST_X_T1, NADJUST_X_T1);
	public static Tuple<String,String> T2_OPT = 
			new Tuple<>(ADJUST_X_T2, NADJUST_X_T2);
	public static Tuple<String,String> T3_OPT = 
			new Tuple<>(ADJUST_X_T3, NADJUST_X_T3);
	public static Tuple<String,String> T4_OPT = 
			new Tuple<>(ADJUST_X_T4, NADJUST_X_T4);
	public static Tuple<String,String> T5_OPT = 
			new Tuple<>(ADJUST_X_T5, NADJUST_X_T5);
	
	public static Tuple<String,String> T1Y_OPT = 
			new Tuple<>(ADJUST_Y_T1, NADJUST_Y_T1);
	public static Tuple<String,String> T2Y_OPT = 
			new Tuple<>(ADJUST_Y_T2, NADJUST_Y_T2);
	public static Tuple<String,String> T3Y_OPT = 
			new Tuple<>(ADJUST_Y_T3, NADJUST_Y_T3);
	public static Tuple<String,String> T4Y_OPT = 
			new Tuple<>(ADJUST_Y_T4, NADJUST_Y_T4);
	public static Tuple<String,String> T5Y_OPT = 
			new Tuple<>(ADJUST_Y_T5, NADJUST_Y_T5); 
	
	public static Tuple<String,String> T1Z_OPT = 
			new Tuple<>(ADJUST_Z_T1, NADJUST_Z_T1);
	public static Tuple<String,String> T2Z_OPT = 
			new Tuple<>(ADJUST_Z_T2, NADJUST_Z_T2);
	public static Tuple<String,String> T3Z_OPT = 
			new Tuple<>(ADJUST_Z_T3, NADJUST_Z_T3);
	public static Tuple<String,String> T4Z_OPT = 
			new Tuple<>(ADJUST_Z_T4, NADJUST_Z_T4);
	public static Tuple<String,String> T5Z_OPT = 
			new Tuple<>(ADJUST_Z_T5, NADJUST_Z_T5); 
	
	public static Tuple<String,Tuple<String,String>> PAIRT2_T1 =
			new Tuple<>(BASE_T1, T1_OPT);
	public static Tuple<String,Tuple<String,String>> PAIRT2_T2 =
			new Tuple<>(BASE_T2, T2_OPT);
	public static Tuple<String,Tuple<String,String>> PAIRT2_T3 =
			new Tuple<>(BASE_T3, T3_OPT);
	public static Tuple<String,Tuple<String,String>> PAIRT2_T4 =
			new Tuple<>(BASE_T4, T4_OPT);
	public static Tuple<String,Tuple<String,String>> PAIRT2_T5 =
			new Tuple<>(BASE_T5, T5_OPT);
	
	public static Tuple<String,List<Tuple<String,String>>> PAIRT3_T1 =
			new Tuple<String,List<Tuple<String,String>>>
				(
						BASE_T1, 
						new ArrayList<Tuple<String,String>>(){{
							add(T1_OPT);
							add(T1Y_OPT);
							add(T1Z_OPT);
						}}
				);
	
	public static Tuple<String,List<Tuple<String,String>>> PAIRT3_T2 =
			new Tuple<String,List<Tuple<String,String>>>
				(
						BASE_T2, 
						new ArrayList<Tuple<String,String>>(){{
							add(T2_OPT);
							add(T2Y_OPT);
							add(T2Z_OPT);
						}}
				);
	
	public static Tuple<String,List<Tuple<String,String>>> PAIRT3_T3 =
			new Tuple<String,List<Tuple<String,String>>>
				(
						BASE_T3, 
						new ArrayList<Tuple<String,String>>(){{
							add(T3_OPT);
							add(T3Y_OPT);
							add(T3Z_OPT);
						}}
				);
	
	public static Tuple<String,List<Tuple<String,String>>> PAIRT3_T4 =
			new Tuple<String,List<Tuple<String,String>>>
				(
						BASE_T4, 
						new ArrayList<Tuple<String,String>>(){{
							add(T4_OPT);
							add(T4Y_OPT);
							add(T4Z_OPT);
						}}
				);
	
	public static Tuple<String,List<Tuple<String,String>>> PAIRT3_T5 =
			new Tuple<String,List<Tuple<String,String>>>
				(
						BASE_T5, 
						new ArrayList<Tuple<String,String>>(){{
							add(T5_OPT);
							add(T5Y_OPT);
							add(T5Z_OPT);
						}}
				);
	
	public static Tuple<List<Equation>, List<Function>> buildEquations() throws Exception{
		if (TYPE == 1) {
			return buildTypeOneEquations();
		} else if (TYPE == 2) {
			return buildTypeTwoEquations();
		} else if (TYPE == 3) {
			return buildTypeThreeEquations();
		}
		throw new Exception("Unknown equation set asked for.");
	}
	
	/*
	 * Builds the dummy equations for example 2 in the paper using approach two.
	 */
	public static Tuple<List<Equation>, List<Function>> buildTypeThreeEquations() {
		List<Equation> eqs = new ArrayList<>();
		List<Function> vars = new ArrayList<>();
		Map<String, Tuple<Function,Integer>> varnames = new HashMap<>();
		VariableCounter counter =  new VariableCounter();
		createVariableNames(varnames, vars, counter, new String[] {"x", "y", "z"});
//		build equations
//		1/1 = bt1 . naxt1_x . axt1_y^30 . axt1_z^30
		eqs.add( 
				createTypeThreeEquation(
			 1.0f/1.0f, new int[]{ 1, 15, 30}, new boolean[] {false, true,true},
			 1,
			 PAIRT3_T1.getLeft(), PAIRT3_T1.getRight(),
			 varnames, vars, counter
			)	
		);
//		1/1 = bt1 . axt1_x^19 . naxt1_y . axt1_z^30
//		      / bt1 . axt1_x^19 . naxt1_y . axt1_z^30
		eqs.add( 
			createTypeThreeEquation(
				1.0f/1.0f, new int[] {19, 1, 30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T1.getLeft(), PAIRT3_T1.getRight(),
				varnames, vars, counter
			)
		);
//		1/1 = bt1 . axt1_x^63 . axt1_y^9 . axt1_z^30
//			/ bt1 . axt1_x^63 . axt1_y^9 . axt1_z^30
		eqs.add( 
			createTypeThreeEquation( 
					1.0f/1.0f, new int[] {63,9,30}, new boolean[] {true,true,true},
					1,
					PAIRT3_T1.getLeft(), PAIRT3_T1.getRight(),
					varnames, vars, counter
			)
		);
//		1/1 = (bt3 . naxt3_x . naxt3_y . axt3_z^30) 
//		    / (bt2 . naxt2_x . naxt2_y . axt2_z^30) + (bt3 . naxt3_x . naxt3_y . axt3_z^30) 
		eqs.add( 
				createTypeThreeEquation( 
						1.0f/1.0f, new int[] {1,1,30}, new boolean[] {false,false,true},
						1,
						PAIRT3_T3.getLeft(), PAIRT3_T3.getRight(),
						varnames, vars, counter,
						PAIRT3_T2
				)
			);
//		1/1 = (bt3 . naxt3_x . axt3_y^35 . axt3_z^30) 
//	    / (bt2 . naxt2_x . axt2_y^35 . axt2_z^30) + (bt3 . naxt3_x . axt3_y^35 . axt3_z^30) 
	eqs.add( 
			createTypeThreeEquation( 
					1.0f/1.0f, new int[] {1,35,30}, new boolean[] {false,true,true},
					1,
					PAIRT3_T3.getLeft(), PAIRT3_T3.getRight(),
					varnames, vars, counter,
					PAIRT3_T2
			)
		);
//	1/1 = (bt2 . naxt2_x . axt2_y^55 . axt2_z^30) 
//    / (bt2 . naxt2_x . axt2_y^55 . axt2_z^30) + (bt3 . naxt3_x . axt3_y^55 . axt3_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {1,55,30}, new boolean[] {false,true,true},
				1,
				PAIRT3_T2.getLeft(), PAIRT3_T2.getRight(),
				varnames, vars, counter,
				PAIRT3_T3
		)
	);
//	1/1 = (bt2 . axt2_x^5 . naxt2_y . axt2_z^30) 
//    / (bt2 . axt3_x^5 . naxt3_y . axt3_z^30) + (bt3 . axt3_x^5 . naxt3_y . axt3_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {5,1,30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T2.getLeft(), PAIRT3_T2.getRight(),
				varnames, vars, counter,
				PAIRT3_T3
		)
	);
//	1/1 = (bt2 . axt2_x^21 . naxt2_y . axt2_z^30) 
//  / (bt2 . axt3_x^21 . naxt3_y . axt3_z^30) + (bt3 . axt3_x^21 . naxt3_y . axt3_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {21,1,30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T2.getLeft(), PAIRT3_T2.getRight(),
				varnames, vars, counter,
				PAIRT3_T3
		)
	);
//	1/1 = (bt3 . axt3_x^36 . axt3_y^33 . axt3_z^30) 
//  / (bt2 . axt2_x^36 . axt2_y^33 . axt2_z^30) + (bt3 . axt3_x^36 . axt3_y^33 . axt3_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {36,33,30}, new boolean[] {true,true,true},
				1,
				PAIRT3_T3.getLeft(), PAIRT3_T3.getRight(),
				varnames, vars, counter,
				PAIRT3_T2
		)
	);
//	1/1 = (bt2 . axt2_x^48 . axt2_y^21 . axt2_z^30) 
//  / (bt2 . axt2_x^48 . axt2_y^21 . axt2_z^30) + (bt3 . axt3_x^48 . axt3_y^21 . axt3_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {48,21,30}, new boolean[] {true,true,true},
				1,
				PAIRT3_T2.getLeft(), PAIRT3_T2.getRight(),
				varnames, vars, counter,
				PAIRT3_T3
		)
	);
//	1/2 = (bt4 . naxt4_x . naxt4_y . axt4_z^30) 
//  / (bt4 . naxt4_x . naxt4_y . axt4_z^30) + (bt5 . naxt5_x . naxt5_y . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/2.0f, new int[] {1,1,30}, new boolean[] {false,false,true},
				1,
				PAIRT3_T4.getLeft(), PAIRT3_T4.getRight(),
				varnames, vars, counter,
				PAIRT3_T5
		)
	);
//	1/2 = (bt5 . naxt5_x . naxt5_y . axt5_z^30) 
//  / (bt4 . naxt4_x . naxt4_y . axt4_z^30) + (bt5 . naxt5_x . naxt5_y . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/2.0f, new int[] {1,1,30}, new boolean[] {false,false,true},
				1,
				PAIRT3_T5.getLeft(), PAIRT3_T5.getRight(),
				varnames, vars, counter,
				PAIRT3_T4
		)
	);
//	1/1 = (bt4 . naxt4_x . axt4_y^55 . axt4_z^30) 
//  / (bt4 . naxt4_x . axt4_y^55 . axt4_z^30) + (bt5 . naxt5_x . axt5_y^55 . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {1,55,30}, new boolean[] {false,true,true},
				1,
				PAIRT3_T4.getLeft(), PAIRT3_T4.getRight(),
				varnames, vars, counter,
				PAIRT3_T5
		)
	);
//	1/1 = (bt5 . axt4_x . naxt4_y . axt4_z^30) 
//  / (bt4 . axt4_x . naxt4_y . axt4_z^30) + (bt5 . axt5_x . naxt5_y . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {1,1,30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T5.getLeft(), PAIRT3_T5.getRight(),
				varnames, vars, counter,
				PAIRT3_T4
		)
	);
//	1/1 = (bt5 . axt4_x^5 . naxt4_y . axt4_z^30) 
//  / (bt4 . axt4_x^5 . naxt4_y . axt4_z^30) + (bt5 . axt5_x^5 . naxt5_y . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {5,1,30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T4.getLeft(), PAIRT3_T4.getRight(),
				varnames, vars, counter,
				PAIRT3_T5
		)
	);
//	1/1 = (bt5 . axt4_x^9 . naxt4_y . axt4_z^30) 
//  / (bt4 . axt4_x^9 . naxt4_y . axt4_z^30) + (bt5 . axt5_x^9 . naxt5_y . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {9,1,30}, new boolean[] {true,false,true},
				1,
				PAIRT3_T5.getLeft(), PAIRT3_T5.getRight(),
				varnames, vars, counter,
				PAIRT3_T4
		)
	);
//	1/1 = (bt5 . axt4_x^36 . axt4_y^33 . axt4_z^30) 
//  / (bt4 . axt4_x^36 . axt4_y^33 . axt4_z^30) + (bt5 . axt5_x^36 . axt5_y^33 . axt5_z^30) 
	eqs.add( 
		createTypeThreeEquation( 
				1.0f/1.0f, new int[] {36,33,30}, new boolean[] {true,true,true},
				1,
				PAIRT3_T4.getLeft(), PAIRT3_T4.getRight(),
				varnames, vars, counter,
				PAIRT3_T5
		)
	);
//		all done!
//		show made equations
		if (SHOW_EQS) {
			for (Equation eq : eqs) {
				System.out.println(eq.toString());
			}
		}
		return new Tuple<>(eqs,vars);
	}
	
	public static String TEMPLATE_BASE_POWER = "base_t_%d";
	public static String TEMPLATE_XADJUST = "adjust_%s_t_%d";
	public static String TEMPLATE_NOT_XADJUST = "not_adjust_%s_t_%d";

	public static void createVariableNames(Map<String, Tuple<Function, Integer>> varnames, List<Function> vars,
			VariableCounter counter, String ...xpowers) {
		for (int i=1; i <6; i++) {
//			create base variable
			createVarIfNeeded(
					String.format(TEMPLATE_BASE_POWER, i), 
					varnames, vars, counter
			);
//			now for each power create adjuster and not adjuster
			for(String power: xpowers) {
				createVarIfNeeded(
						String.format(TEMPLATE_XADJUST, power, i), 
						varnames, vars, counter
				);
				createVarIfNeeded(
						String.format(TEMPLATE_NOT_XADJUST, power, i), 
						varnames, vars, counter
				);
			}
		}
	}

	/*
	 * Builds the dummy equations in the paper using approach two.
	 */
	public static Tuple<List<Equation>, List<Function>> buildTypeTwoEquations() {
		List<Equation> eqs = new ArrayList<>();
		List<Function> vars = new ArrayList<>();
		Map<String, Tuple<Function,Integer>> varnames = new HashMap<>();
		VariableCounter counter =  new VariableCounter();
//		build dummy equations for type two
//		1/1 = (bt1 . naxt1) / (bt1 . naxt1)
		eqs.add( 
			createTypeTwoEquation(
					1.0/1.0, 1, false, 1,
					BASE_T1, T1_OPT, 
					varnames, vars, counter)	
		);
//		1/1 = (bt1 . axt1^19) / (bt1 . axt1^19)
		eqs.add( 
				createTypeTwoEquation(
						1.0/1.0, 19, true, 1,
						BASE_T1, T1_OPT, 
						varnames, vars, counter)	
			);
//		1/1 = (bt1 . axt1^63) / (bt1 . axt1^63)
		eqs.add( 
				createTypeTwoEquation(
						1.0/1.0, 63, true, 1,
						BASE_T1, T1_OPT, 
						varnames, vars, counter)	
			);
//		1/3 = (bt2 . naxt2) / (bt2 . naxt2 + bt3 . naxt3)
		eqs.add( 
				createTypeTwoEquation(
						1.0/3.0, 1, false, 1,
						BASE_T2, T2_OPT, 
						varnames, vars, counter,
						PAIRT2_T3)	
			);
//		2/3 = (bt3 . naxt2) / (bt2 . naxt2 + bt3 . naxt3)
		eqs.add( 
				createTypeTwoEquation(
						2.0/3.0, 1, false, 2,
						BASE_T3, T3_OPT, 
						varnames, vars, counter,
						PAIRT2_T2)	
			);
//		1/1 = (bt2 . axt2^5) / (bt2 . axt2^5 + bt3 . axt3^5)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 5, true, 1,
						BASE_T2, T2_OPT, 
						varnames, vars, counter,
						PAIRT2_T3)
		);
//		1/1 = (bt2 . axt2^21) / (bt2 . axt2^21 + bt3 . axt3^21)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 21, true, 1,
						BASE_T2, T2_OPT, 
						varnames, vars, counter,
						PAIRT2_T3)
		);
//		1/1 = (bt3 . axt3^36) / (bt2 . axt2^36 + bt3 . axt3^36)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 36, true, 1,
						BASE_T3, T3_OPT, 
						varnames, vars, counter,
						PAIRT2_T2)
		);
//		1/1 = (bt2 . axt2^48) / (bt2 . axt2^48 + bt3 . axt3^48)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 48, true, 1,
						BASE_T2, T2_OPT, 
						varnames, vars, counter,
						PAIRT2_T3)
		);
//		2/3 = (bt4 . naxt4) / (bt4 . naxt4 + bt5 . naxt5)
		eqs.add( 
				createTypeTwoEquation(
						2.0/3.0, 1, false, 2,
						BASE_T4, T4_OPT, 
						varnames, vars, counter,
						PAIRT2_T5)	
		);
//		1/3 = (bt4 . naxt4) / (bt4 . naxt4 + bt5 . naxt5)
		eqs.add( 
				createTypeTwoEquation(
						1.0/3.0, 1, false, 1,
						BASE_T5, T5_OPT, 
						varnames, vars, counter,
						PAIRT2_T4)	
		);
//		1/1 = (bt5 . axt5^1) / (bt4 . axt4^1 + bt5 . axt5^1)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 1, true, 1,
						BASE_T5, T5_OPT, 
						varnames, vars, counter,
						PAIRT2_T4)
		);
//		1/1 = (bt4 . axt4^5) / (bt4 . axt4^5 + bt5 . axt5^5)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 5, true, 1,
						BASE_T4, T4_OPT, 
						varnames, vars, counter,
						PAIRT2_T5)
		);
//		1/1 = (bt5 . axt5^9) / (bt4 . axt4^9 + bt5 . axt5^9)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 9, true, 1,
						BASE_T5, T5_OPT, 
						varnames, vars, counter,
						PAIRT2_T4)
		);
//		1/1 = (bt4 . axt4^36) / (bt4 . axt4^36 + bt5 . axt5^36)
		eqs.add(
				createTypeTwoEquation(
						1.0/1.0, 36, true, 1,
						BASE_T4, T4_OPT, 
						varnames, vars, counter,
						PAIRT2_T5)
		);
//		show made equations
		if (SHOW_EQS) {
			for (Equation eq : eqs) {
				System.out.println(eq.toString());
			}
		}
		return new Tuple<List<Equation>, List<Function>>(eqs,vars);
	}

	/*
	 * Builds the dummy equations in paper using approach one.
	 */
	public static Tuple<List<Equation>,List<Function>> buildTypeOneEquations() {
		List<Equation> eqs = new ArrayList<>();
		List<Function> vars = new ArrayList<>();
		Map<String, Tuple<Function,Integer>> varnames = new HashMap<>();
		VariableCounter counter =  new VariableCounter();
//		first equation
//		1/1 = (base_t_1 . adjust_t_1^0)/ (base_t_1 . adjust_t_1^0)
		eqs.add(
				createEquation(1/1.0f, 0, BASE_T1, ADJUST_X_T1, varnames, vars, counter)
		);
//		1/1 = (base_t_1 . adjust_t_1^19)/ (base_t_1 . adjust_t_1^19)
		eqs.add(
				createEquation(1/1.0f, 19, BASE_T1, ADJUST_X_T1, varnames, vars, counter)
		);
//		1/1 = (base_t_1 . adjust_t_1^63)/ (base_t_1 . adjust_t_1^63)
		eqs.add(
				createEquation(1/1.0f, 63, BASE_T1, ADJUST_X_T1, varnames, vars, counter)
		);
//		1/2 = (base_t_2 . adjust_t_2^0) / ( base_t_2 . adjust_t_2^0 + base_t_3 . adjust_t_3^0)
		eqs.add( 
				createEquation(1/2.0f, 0,
						BASE_T2, ADJUST_X_T2, varnames, vars, counter,
						new Tuple<>(BASE_T3,ADJUST_X_T3))
		);
//		1/2 = (base_t_3 . adjust_t_3^0) / ( base_t_2 . adjust_t_2^0 + base_t_3 . adjust_t_3^0)
		eqs.add( 
				createEquation(1/2.0f, 0,
						BASE_T3, ADJUST_X_T3, varnames, vars, counter,
						new Tuple<>(BASE_T2,ADJUST_X_T2))
		);
//		1/1 = (base_t_2 . adjust_t_2^5) / ( base_t_2 . adjust_t_2^5 + base_t_3 . adjust_t_3^5)
		eqs.add( 
				createEquation(1/1.0f, 5,
						BASE_T2, ADJUST_X_T2, varnames, vars, counter,
						new Tuple<>(BASE_T3,ADJUST_X_T3))
		);
//		1/1 = (base_t_2 . adjust_t_2^21) / ( base_t_2 . adjust_t_2^21 + base_t_3 . adjust_t_3^21)
		eqs.add( 
				createEquation(1/1.0f, 21,
						BASE_T2, ADJUST_X_T2, varnames, vars, counter,
						new Tuple<>(BASE_T3,ADJUST_X_T3))
		);
//		1/1 = (base_t_3 . adjust_t_3^22) / ( base_t_2 . adjust_t_2^22 + base_t_3 . adjust_t_3^22)
		eqs.add( 
				createEquation(1/1.0f, 22,
						BASE_T3, ADJUST_X_T3, varnames, vars, counter,
						new Tuple<>(BASE_T2,ADJUST_X_T2))
		);
//		1/1 = (base_t_3 . adjust_t_3^36) / ( base_t_2 . adjust_t_2^36 + base_t_3 . adjust_t_3^36)
		eqs.add( 
				createEquation(1/1.0f, 36,
						BASE_T3, ADJUST_X_T3, varnames, vars, counter,
						new Tuple<>(BASE_T2,ADJUST_X_T2))
		);
//		1/1 = (base_t_2 . adjust_t_2^48) / ( base_t_2 . adjust_t_2^48 + base_t_3 . adjust_t_3^48)
		eqs.add( 
				createEquation(1/1.0f, 48,
						BASE_T2, ADJUST_X_T2, varnames, vars, counter,
						new Tuple<>(BASE_T3,ADJUST_X_T3))
		);
//		1/2 = (base_t4 . adjust_t_4^0) / (base_t4 . adjust_t_4^0 + base_t5 . adjust_t_5^0)
		eqs.add( 
				createEquation(1/2.0f, 0,
						BASE_T4, ADJUST_X_T4, varnames, vars, counter,
						new Tuple<>(BASE_T5, ADJUST_X_T5)
				)
		);
//		1/2 = (base_t5 . adjust_t_5^0) / (base_t4 . adjust_t_4^0 + base_t5 . adjust_t_5^0)
		eqs.add( 
				createEquation(1/2.0f, 0,
						BASE_T5, ADJUST_X_T5, varnames, vars, counter,
						new Tuple<>(BASE_T4, ADJUST_X_T4)
				)
		);
//		1/1 = (base_t5 . adjust_t_5^1) / (base_t4 . adjust_t_4^1 + base_t5 . adjust_t_5^1)
		eqs.add( 
				createEquation(1/1.0f, 1,
						BASE_T5, ADJUST_X_T5, varnames, vars, counter,
						new Tuple<>(BASE_T4, ADJUST_X_T4)
				)
		);
//		1/1 = (base_t4 . adjust_t_4^5) / (base_t4 . adjust_t_4^5 + base_t5 . adjust_t_5^5)
		eqs.add( 
				createEquation(1/1.0f, 5,
						BASE_T4, ADJUST_X_T4, varnames, vars, counter,
						new Tuple<>(BASE_T5, ADJUST_X_T5)
				)
		);
//		1/1 = (base_t5 . adjust_t_5^9) / (base_t4 . adjust_t_4^9 + base_t5 . adjust_t_5^9)
		eqs.add( 
				createEquation(1/1.0f, 9,
						BASE_T5, ADJUST_X_T5, varnames, vars, counter,
						new Tuple<>(BASE_T4, ADJUST_X_T4)
				)
		);
//		1/1 = (base_t_4 . adjust_t_4^22) / (base_t_4 . adjust_t_4^22) + base_t_5 . adjust_t_5^22)
		eqs.add(
				createEquation(1/1.0f, 22,
						BASE_T4, ADJUST_X_T4, varnames, vars, counter,
						new Tuple<>(BASE_T5, ADJUST_X_T5)
				)
		);
//		1/1 = (base_t_4 . adjust_t_4^36) / (base_t_4 . adjust_t_4^36) + base_t_5 . adjust_t_5^36)
		eqs.add(
				createEquation(1/1.0f, 36,
						BASE_T4, ADJUST_X_T4, varnames, vars, counter,
						new Tuple<>(BASE_T5, ADJUST_X_T5)
				)
		);
		return new Tuple<List<Equation>, List<Function>>(eqs,vars);
	}
	
	@SafeVarargs
	public static Equation createTypeThreeEquation(
			double value, int[] power, boolean[] known,
			int occurances,
			String topBase, List<Tuple<String,String>> topAdjust,
			Map<String, Tuple<Function,Integer>> lookup, 
			List<Function> storage, 
			VariableCounter counter,
			Tuple<String,List<Tuple<String,String>>>... Bottoms) {
		
//		build top (always the same)
//		get functions for variables
		Function b1 = createVarIfNeeded(topBase, lookup, storage, counter);
		Function x1 = null;
		for(int i=0 ; i < known.length; i++) {
//			cycle through adjustments
			boolean knower = known[i];
			String ajname = knower ?
				topAdjust.get(i).getLeft() :
				topAdjust.get(i).getRight();
			int ajpower = knower ? power[i] : 1;
			Function adjuster = createVarIfNeeded(ajname, lookup, storage, counter, ajpower);
			if (x1 == null) {
//				first adjustment
				x1 = adjuster;
			} else {
//				for every other make a chain of products
				x1 = new Product(x1, adjuster);
			}
		}
		Product top = new Product(b1 , x1);
//		build bottom (different)
		Sum bottom = new Sum(top);
		for (int i = 0; i < Bottoms.length; i++) {
			String botBase = Bottoms[i].getLeft();
			Function adjusters = null;
			for (int j=0; j < Bottoms[i].getRight().size(); j++) {
				boolean knower = known[j];
				String ajname = knower ? 
					Bottoms[i].getRight().get(j).getLeft() :
					Bottoms[i].getRight().get(j).getRight();
				int apower = knower ? power[j] : 1;
				Function  adjuster = createVarIfNeeded(ajname, lookup, storage, counter, apower);
				if (adjusters == null) {
					adjusters = adjuster;
				} else {
					adjusters = new Product(adjusters, adjuster);
				}
			}
			bottom = new Sum(bottom,
				new Product( 
						createVarIfNeeded(botBase, lookup, storage, counter),
						adjusters
				)
			);
		}
		Division right = new Division(top, bottom);
//		set value for right
		return new Equation(value, right, occurances);
	}	
	
	@SafeVarargs
	public static Equation createTypeTwoEquation(
			double value, int power, boolean known,
			int occurances,
			String topBase, Tuple<String,String> topAdjust,
			Map<String, Tuple<Function,Integer>> lookup, 
			List<Function> storage, 
			VariableCounter counter,
			Tuple<String,Tuple<String,String>>... Bottoms) {
		
//		build top (always the same)
		String topvar = known ? 
				topAdjust.getLeft() : 
				topAdjust.getRight();
		power = known ? power : 1;
//		get functions for variables
		Function b1 = createVarIfNeeded(topBase, lookup, storage, counter);
		Function x1 = createVarIfNeeded(topvar, lookup, storage, counter, power);
		Product top = new Product(b1 , x1);
//		build bottom (different)
		Sum bottom = new Sum(top);
		for (int i = 0; i < Bottoms.length; i++) {
			String botBase = Bottoms[i].getLeft();
			String botAdjust = known ? 
						Bottoms[i].getRight().getLeft() : 
						Bottoms[i].getRight().getRight();
			bottom = new Sum(bottom,
				new Product( 
						createVarIfNeeded(botBase, lookup, storage, counter),
						createVarIfNeeded(botAdjust, lookup, storage, counter, power)
				)
			);
		}
		Division right = new Division(top, bottom);
//		set value for right
		return new Equation(value, right, occurances);
	}
	
	@SafeVarargs
	public static Equation createEquation(
			double value, int power, 
			String topBase, String topAdjust,
			Map<String, Tuple<Function,Integer>> lookup, 
			List<Function> storage, 
			VariableCounter counter,
			Tuple<String,String>... Bottoms) {
		
//		build top (always the same)
		Function b1 = createVarIfNeeded(topBase, lookup, storage, counter);
		Function x1 = createVarIfNeeded(topAdjust, lookup, storage, counter, power);
		Product top = new Product(b1 , x1);
//		build bottom (different)
		Sum bottom = new Sum(top);
		for (int i = 0; i < Bottoms.length; i++) {
			String botBase = Bottoms[i].getLeft();
			String botAdjust = Bottoms[i].getRight();
			bottom = new Sum(bottom,
				new Product( 
						createVarIfNeeded(botBase, lookup, storage, counter),
						createVarIfNeeded(botAdjust, lookup, storage, counter, power)
				)
			);
		}
		Division right = new Division(top, bottom);
//		set value for right
		return new Equation(value, right, 1);
	}
	
	public static Function createVarIfNeeded(String name, Map<String, Tuple<Function,Integer>> lookup, List<Function> storage, VariableCounter var_count){
		return createVarIfNeeded(name, lookup, storage, var_count, 1);
	}
	
	public static Function createVarIfNeeded(String name, Map<String, Tuple<Function,Integer>> lookup, List<Function> storage, VariableCounter var_count, int power) {
		Function out;
		
		if (lookup.containsKey(name)) {
			if (name.contains("adjust")) {
				Tuple<Function,Integer> old = lookup.get(name);
				out = new VariablePower(old.getRight(), name, power);
			} else {
				out = lookup.get(name).getLeft();
			}
		} else {
			if (name.contains("adjust")) {
				out = new VariablePower(var_count.get(),name, power);
				lookup.put(name, new Tuple<>(out, var_count.get()));
			} else {
				out = new Variable(var_count.get(), name);
				lookup.put(name, new Tuple<>(out, var_count.get()));
			}
			storage.add(out);
			var_count.incr();
		}
		
		return out;
	}
	
}
