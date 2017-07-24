package org.chemcalc.core.util;

import java.util.HashMap;


public class Gaussian {

	private static HashMap<String,AGaussian> gaussians=new HashMap<String,AGaussian>();
	
	final static boolean DEBUG=false;
	
	public static AGaussian getGaussian(int numberPoints, int halfHeightWidth) {
		String key=numberPoints+"-"+halfHeightWidth;
		if (gaussians.containsKey(key)) return gaussians.get(key);
		AGaussian result=new AGaussian(numberPoints, halfHeightWidth);
		gaussians.put(key, result);
		return result;
	}
	

	public static double[][] combineGaussian(double[][] values, AGaussian gaussian, double resolution) {
		int ratio=((gaussian.numberPoints-1)/gaussian.halfHeightWidth)/2;
		double minX=values[0][0];
		double maxX=values[values.length-1][0];
		int slots=(int)(Math.round((maxX-minX)/resolution*gaussian.halfHeightWidth))+2*gaussian.halfHeightWidth*ratio+1;
		double[] ys=new double[slots];
		if (DEBUG) System.out.println("Number of slots: "+slots);
		for (double[] value : values) {
			int currentSlot=(int)(Math.round((value[0]-minX)/resolution*gaussian.halfHeightWidth+gaussian.halfHeightWidth*ratio));
			for (int i=0; i<gaussian.ys.length; i++) {
				ys[currentSlot+i-gaussian.halfHeightWidth*ratio]+=gaussian.ys[i]*value[1];
			}
		}
		double[][] result=new double[slots][2];
		for (int i=0; i<slots; i++) {
			result[i][0]=((double)(i)/gaussian.halfHeightWidth-ratio)*resolution+minX;
			result[i][1]=ys[i];
		}
		return result;
	}

	static double[][] test1={ {0,10}, {2.5,10}, {5,10} };
	static double[][] test2={{383.0,0},{383.63,100},{384.0,0.0}};
	
	public static void main(String[] args) {
		AGaussian gaussian=Gaussian.getGaussian(61, 10);
		double[][] result=Gaussian.combineGaussian(test1, gaussian, 0.01);
		for (int i=0; i<result.length; i++) {
			System.out.println(result[i][0]+"\t"+result[i][1]);
		}
	}
}

