package org.chemcalc.test;

import org.chemcalc.core.*;
import org.chemcalc.core.util.AGaussian;
import org.chemcalc.core.util.Gaussian;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

public class CheckApproximation2 {

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


		String mf="Ru10";
		double resolution1=0.001;
		double resolution2=0.00001;
		double targetResolution=0.01;
		
		Formula formula=new Formula(mf,elements,groups);
	
  		SpectrumCalculator spectrumCalculator=new SpectrumCalculator(elements,formula,resolution1);
     	spectrumCalculator.calculate();
	   	spectrumCalculator.normalize();
	   	spectrumCalculator.getIsotopicDistribution().put(Math.floor(spectrumCalculator.getIsotopicDistribution().getMinX()), new Double(0));
	   	spectrumCalculator.getIsotopicDistribution().put(Math.ceil(spectrumCalculator.getIsotopicDistribution().getMaxX()), new Double(0));
	   	
	   	AGaussian gaussian=Gaussian.getGaussian(61, 10);
		spectrumCalculator.applyGaussian(gaussian, targetResolution);
		double [][] result1=spectrumCalculator.toXYArray();
		standardize(result1);
		
//		result1=combine(result1, targetResolution);
//		printDoubleArray(result1);

		SpectrumCalculator spectrumCalculator2=new SpectrumCalculator(elements,formula,resolution2);
		spectrumCalculator2.calculate();
		spectrumCalculator2.normalize();
		spectrumCalculator2.getIsotopicDistribution().put(Math.floor(spectrumCalculator2.getIsotopicDistribution().getMinX()), new Double(0));
		spectrumCalculator2.getIsotopicDistribution().put(Math.ceil(spectrumCalculator2.getIsotopicDistribution().getMaxX()), new Double(0));
		spectrumCalculator2.applyGaussian(gaussian, targetResolution);
		double [][] result2=spectrumCalculator2.toXYArray();
		standardize(result2);
		
	//	result2=combine(result2, targetResolution);
	//	printDoubleArray(result2);
		for (int i=0; i<10000; i++) {
			System.out.println(result1[i][0]+"\t"+result2[i][0]+"\t"+result1[i][1]+"\t"+result2[i][1]);
		}
		
		double difference=0;
		double total=0;
		for (int i=0; i<result1.length; i++) {
			difference+=Math.abs(result1[i][1]-result2[i][1]);
			total+=result1[i][1];
		}
		System.out.println("Difference sum: "+difference);
		System.out.println("Total sum: "+total);
		
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
		System.out.println("------------ >"+sum);
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
