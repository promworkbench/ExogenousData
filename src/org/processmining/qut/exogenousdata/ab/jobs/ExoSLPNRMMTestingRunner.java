package org.processmining.qut.exogenousdata.ab.jobs;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class ExoSLPNRMMTestingRunner {

	public static String outFile = "C:\\Users\\adam\\Desktop\\testing\\BPM2024B-5-RF-norm-global.stdout";
	
	public static void main(String[] args) throws FileNotFoundException {
		System.setOut(new PrintStream(
				new BufferedOutputStream(
						new FileOutputStream(outFile,true)), 
				true));
		new ExoSLPNRMMTesting().run();
	}

}