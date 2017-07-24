package org.chemcalc.core.util;

public class AGaussian {

	int numberPoints=0;
	int halfHeightWidth=0;
	double[] ys=null;
	
	AGaussian (int numberPoints, int halfHeightWidth) {
		this.numberPoints=numberPoints;
		this.halfHeightWidth=halfHeightWidth;
		double sigma=halfHeightWidth/(2*Math.sqrt(2*Math.log(2)));
		ys=new double[numberPoints];
		for (int i=0; i<numberPoints; i++) {
			double x=i-((numberPoints-1)/2);	
			ys[i]=1/(sigma*Math.sqrt(2*Math.PI)) * Math.exp(-x*x/(2*sigma*sigma));
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
