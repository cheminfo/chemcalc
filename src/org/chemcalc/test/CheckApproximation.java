package org.chemcalc.test;

import org.chemcalc.core.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class CheckApproximation {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MFException 
	 */
	public static void main(String[] args) throws IOException, MFException {
		// TODO Auto-generated method stub
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();
		HashMap<String,String> options=new HashMap<String,String>();
		boolean init=false;
		elements = LoadFromFiles.elements("data/atom2012.txt","data/isotope2012.txt");
		groups=LoadFromFiles.groups("data/group.txt",elements);


		String mf="Ru4";
		double resolution1=0.001;
		double resolution2=0.00001;
		double targetResolution=0.001;
		
		Formula formula=new Formula(mf,elements,groups);
	
  		SpectrumCalculator spectrumCalculator=new SpectrumCalculator(elements,formula,resolution1);
     	spectrumCalculator.calculate();
	   	spectrumCalculator.normalize();
		double [][] result1=spectrumCalculator.toXYArray();

		
		standardize(result1);
		
		result1=combine(result1, targetResolution);
		printDoubleArray(result1);

 		spectrumCalculator=new SpectrumCalculator(elements,formula,resolution2);
     	spectrumCalculator.calculate();
	   	spectrumCalculator.normalize();
		double [][] result2=spectrumCalculator.toXYArray();
		
		standardize(result2);
		
		
		result2=combine(result2, targetResolution);
		printDoubleArray(result2);
		
	}
	
	static void printDoubleArray(double[][] result) {
		for (double[] value : result) {
			System.out.println(value[0]+"\t"+value[1]);
		}
	}
	

	static void standardize(double[][] original) {
		double sum=0;
		for (double[] value : original) {
			sum+=value[1];
		}
		sum/=100;
		for (int i=0; i<original.length; i++) {
			original[i][1]/=sum;
		}
	}
	
	static double[][] combine(double[][] original, double resolution) {
		TreeMap<Double,Double> result=new TreeMap<Double,Double>();
		
		for (double[] value : original) {
			double slot=Math.round(value[0]/(resolution))*resolution;
			if (result.containsKey(slot)) {
				result.put(slot, result.get(slot)+value[1]);
			} else {
				result.put(slot, value[1]);
			}
		}
		
		
		double[][] toReturn=new double[result.size()][2];
		int i=0;
		for (Double key : result.keySet()) {
			toReturn[i][0]=key;
			toReturn[i][1]=result.get(key);
			i++;
		}
		return toReturn;
	}
	
	
}
