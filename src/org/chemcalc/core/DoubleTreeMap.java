package org.chemcalc.core;


import java.math.BigDecimal;
import java.util.*;

/**
 * Deal with special TreeMap containing Double / Double values.
 *
 * version 1.0 (Feb 13, 2004)
 *
 * @author Luc Patiny
 * @author Michal Krompiec
 * @author Marek Noga
 */

public class DoubleTreeMap extends TreeMap<Double,Double>  {
	
	/** Supress all the values of the TreeMap that are under the intensityCutoff
	 */


	private int maxSize=10000;
	
	public void cutoff(double cutoff) {
	 	Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			double y=((Double)value.getValue()).doubleValue();
			if (y<cutoff) iterator.remove();
		}
	}
	
	public void setMaxSize(int maxSize) {
		this.maxSize=maxSize;
	}
	
	public int getMaxSize() {
		return maxSize;
	}
	
	public void resize() {
		int max=this.maxSize/2;
		if (this.size()<max) return;
	//	System.out.println(System.currentTimeMillis()+" - Begining resizing: "+this.size());
        TreeSet<Map.Entry<Double,Double>> set = new TreeSet(new Comparator() {
            public int compare(Object obj, Object obj1) {
                return -((Comparable) ((Map.Entry<Double,Double>) obj).getValue()).compareTo(((Map.Entry<Double,Double>) obj1).getValue());
            }
        });
        set.addAll(this.entrySet());
        int i=0;
        Map.Entry<Double,Double> entry;
    //    System.out.println(System.currentTimeMillis()+" - Starting remove: "+this.size());
        for (Iterator<Map.Entry<Double,Double>> it = set.iterator(); it.hasNext();) {
        	entry = it.next();
        	// System.out.println(entry.getKey() + " - " + entry.getValue());
        	if (i++>max) {
        		this.remove(entry.getKey());
        	}
        }
   //     System.out.println(System.currentTimeMillis()+" - End resizing: "+this.size());
	}
	
	
	
	/**
	 * Combines all the X values that are at less than resolution from each other
	 */
	
	public void combine (double resolution) {
		// LP : seems really badly written but could not find a better way
		DoubleTreeMap newTreeMap=new DoubleTreeMap();
		Iterator<Map.Entry<Double,Double>> iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Double,Double> value=(Map.Entry<Double,Double>)iterator.next();
			double x=value.getKey();
			double y=value.getValue();
			x=Math.rint(x/resolution)*resolution;
			
			Double existingY=newTreeMap.get(new Double(x));
			
			if (existingY!=null) {
				y+=existingY.doubleValue();
			}
			newTreeMap.put(new Double(x),new Double(y));
		}
		this.clear();
		this.add(newTreeMap);
	}

	/**
	 * Combines all the X values that are at less than resolution from each other
	 */

	public void combineProportional (double resolution) {
		DoubleTreeMap newTreeMap=new DoubleTreeMap();
		Iterator<Map.Entry<Double,Double>> iterator=this.entrySet().iterator();

		if (! iterator.hasNext()) {
			return;
		}

		double previousX=Double.NEGATIVE_INFINITY;
		double previousY=0;
		Map.Entry<Double,Double> value;

		while (iterator.hasNext()) {
			value=iterator.next();
			double x=value.getKey();
			double y=value.getValue();
			if ( (x-previousX) > resolution) { // need to add the result
				if (previousX!=Double.NEGATIVE_INFINITY) {
					newTreeMap.put(new Double(previousX), new Double(previousY));
				}
				previousX = x;
				previousY = y;
			} else {
				// need to make the average
				previousX=(previousX*previousY+x*y)/(previousY+y);
				previousY+=y;
			}
		}
		if (previousX!=Double.NEGATIVE_INFINITY) {
			newTreeMap.put(new Double(previousX), new Double(previousY));
		}

		this.clear();
		this.add(newTreeMap);
	}


	public double getXforMaxY () {
		double biggestValue=Double.MIN_VALUE;
		double xValue=0;
		Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			double y=((Double)value.getValue()).doubleValue();
			if (y>biggestValue) {
				biggestValue=y;
				xValue=x.doubleValue();
			}
		}
		return xValue;
	}

	/** Set the maximum Y value to 1
	 */
	
	public void normalize() {
		normalize(1);
	}
	
	/** Set the maximum Y value to maxValue
	 */
	
	public void normalize (double maxValue) {
		double biggestValue=Double.MIN_VALUE;
		Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			double y=((Double)value.getValue()).doubleValue();
			if (y>biggestValue) biggestValue=y;
		}
		iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			Double y=(Double)value.getValue();
			this.put(x,new Double(y.doubleValue()/biggestValue*maxValue));
		}
	}
	
	public double getXForMaxValue() {
		double biggestValue=Double.MIN_VALUE;
		double xForMax=0;
		Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			double y=((Double)value.getValue()).doubleValue();
			if (y>biggestValue) {
				biggestValue=y;
				xForMax=x;
			}
		}
		return xForMax;
	}
	
	
	/** Add value to all the Y.
	 */
	private void addY (double toAdd) {
	 	Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			Double y=(Double)value.getValue();
			this.put(x,new Double(y.doubleValue()+toAdd));
		}
	}
	
	/** Add value to all the X.
	 */
	public void addX (double toAdd) {
		// LP : again it looks like a stupid way
		DoubleTreeMap newTreeMap=new DoubleTreeMap();
	 	Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			Double y=(Double)value.getValue();
			newTreeMap.put(new Double(x.doubleValue()+toAdd), y);
			iterator.remove();
		}
		this.add(newTreeMap);
	}
	
		
	/** Multiply all the X values by value
	 */
	private void multiplyX (double multiplyFactor) {
		// LP : again it looks like a stupid way
		DoubleTreeMap newTreeMap=new DoubleTreeMap();
	 	Iterator iterator=this.entrySet().iterator();
	 	Double x;
	 	Double y;
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			x=(Double)value.getKey();
			y=(Double)value.getValue();
			newTreeMap.put(new Double(x.doubleValue()*multiplyFactor), y);
			iterator.remove();
		}
		this.add(newTreeMap);
	}
	

	
	/** Multiply all the Y values by value
	 */
	public void multiplyY (double multiplyFactor) {
	 	Iterator iterator=this.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry value=(Map.Entry)iterator.next();
			Double x=(Double)value.getKey();
			Double y=(Double)value.getValue();
			this.put(x,new Double(y.doubleValue()*multiplyFactor));
		}
	}
	
	/** Divide all the Y values by value
	 */
	public void divideY (double divideFactor) {
		this.multiplyY (1/divideFactor);	
	}
	
	/** Divide all the Y values by value
	 */
	public void divideX (double divideFactor) {
		this.multiplyX (1/divideFactor);	
	}
	
	/** Make the sum of 2 TreeMaps
	 */
	public void add(TreeMap toAddTreeMap) {
		Iterator iterator=toAddTreeMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry=(Map.Entry)iterator.next();
			Double x=((Double)entry.getKey());
			Double y=((Double)entry.getValue());

			Double existingY=(Double)this.get(x);
			if (existingY!=null) {
				y=new Double(existingY.doubleValue()+y.doubleValue());
			}
			this.put(x,y);
		}
	}

	
	
	public Double getMinX() {
		return (Double)this.firstKey();
	}
	
	public Double getMaxX() {
		return (Double)this.lastKey();
	}

	/**	Get X that coresponds to max Y value
	 */
	public Double getXOfMaxY() {
		Double toReturn = new Double(0);
		Double maxY = new Double(0);
		Iterator iterator=this.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry=(Map.Entry)iterator.next();
			Double x=((Double)entry.getKey());
			Double y=((Double)entry.getValue());
			if (y.doubleValue()>maxY.doubleValue()){ 
				maxY=y;
				toReturn=x;
			}
		}
		return toReturn;
	}
	
	public StringBuffer toMassJcamp(String title, HashMap<Double, String> labels, HashMap<String,String> extraInfo) {
		
	
		/*
		 * 		DecimalFormat format=new DecimalFormat("#########.########");
		DecimalFormatSymbols dfs=new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		format.setGroupingSize(50);
		format.setDecimalFormatSymbols(dfs);
		
		 */
		
		StringBuffer toReturn=new StringBuffer();
		//for (Double value : labels.keySet()) {
		//	System.out.println(value+" - "+labels.get(value));
		//}


		int size=this.size();
		if (size>(maxSize/2)) size=maxSize/2;
		
		toReturn.append("##TITLE= "+title+"\r\n"
     		+"##JCAMP-DX= 5.00\r\n"
     		+"##DATA TYPE= MASS SPECTRUM\r\n"
     		+"##DATA CLASS= PEAK TABLE\r\n"
     		+"##ORIGIN= Generated spectrum based on ChemCalc www.chemcalc.org\r\n"
     		+"##SPECTROMETER/DATA SYSTEM= Based on ChemCalc isotopic distribution calculator\r\n"
     		+"##XUNITS= M/Z\r\n"
     		+"##YUNITS= RELATIVE ABUNDANCE\r\n"
     		+"##NPOINTS=         \r\n");
		int begin=toReturn.length();
		
     	toReturn.append("##MAXY= 100\r\n"
     		+"##MINY= 0\r\n");
		
		if (extraInfo!=null) {
			for (String key : extraInfo.keySet()) {
				toReturn.append("##$"+key+"="+extraInfo.get(key)+"\r\n");
			}
		}
		
	    toReturn.append("##PEAK TABLE= (XY..XY)\r\n");
	    
	    int nbPoints=0;
	    
	    
     	Iterator iterator=this.entrySet().iterator();
     	
     	Double[] labelKeys=labels.keySet().toArray(new Double[labels.keySet().size()]);
     	Arrays.sort(labelKeys);
     	int currentLabelPosition=0;
     	double previousValue=Double.MIN_VALUE;
     	
     	// we should not put 3 Y=0 after each other, first and last are enough
     	
     	boolean firstData=true;
     	int counter=0;
     	boolean previousYIsZero=false;
     	String zeroLine="";
     	while (iterator.hasNext() && counter<size) {
     		counter++;
     		Map.Entry entry=(Map.Entry)iterator.next();
     		double x=((Double)entry.getKey()).doubleValue();
     		double y=((Double)entry.getValue()).doubleValue();
     		if (y<1e-9) y=0;
     		
     		// is the label closest to the previous one ?
      		// we repeat the process ...
     		if (y!=0 && zeroLine.length()>0) {
     			toReturn.append(zeroLine);
     			nbPoints++;
     			zeroLine="";
     		}
     		
 			boolean first=true;
 			for (int i=currentLabelPosition; i<labelKeys.length; i++) {
 	    		if (! iterator.hasNext() || Math.abs(labelKeys[i]-previousValue)<(Math.abs(labelKeys[i]-x))) {
     				if (first) {
     					toReturn.append("  $$ ");
     					first=false;
     				} else {
     					toReturn.append(",");
     				}
     				toReturn.append(labels.get(labelKeys[i]));
	     			currentLabelPosition++;
 	    		} else {
 	    			break;
 	    		}
 			}
     		previousValue=x;
     		if (firstData) {
     			firstData=false;
     			toReturn.append(toFixed(x)+", "+toFixed(y*100));
     			nbPoints++;
     		} else {
     			if (!previousYIsZero || y!=0) {
	     			toReturn.append("\r\n");
	     			toReturn.append(toFixed(x)+", "+toFixed(y*100));
	     			nbPoints++;
     			} else {
     				zeroLine="\r\n"+toFixed(x)+", "+toFixed(y*100);
     			}
     			if (y==0) {
     				previousYIsZero=true;
     			} else {
     				previousYIsZero=false;
     			}
     		}
     		         	
        } 
     	toReturn.append("\r\n##END=\r\n");
     	
     	toReturn.replace(begin-10, begin-2, nbPoints+"");
     	
     	return toReturn;
	}
	
	public StringBuffer toXY() {
		
		int size=this.size();
		if (size>(maxSize/2)) size=maxSize/2;

		StringBuffer toReturn=new StringBuffer();

		Iterator iterator=this.entrySet().iterator();
     	int counter=0;;
     	while (iterator.hasNext() && counter<size) {
     		counter++;
     		Map.Entry entry=(Map.Entry)iterator.next();
     		double x=((Double)entry.getKey()).doubleValue();
     		double y=((Double)entry.getValue()).doubleValue();
     		toReturn.append(toFixed(x)+", "+toFixed(y*100)+"\r\n");   
    	}
    	return toReturn;
	}

	public double[][] toXYArray() {
		
		int size=this.size();
		if (size>(maxSize/2)) size=maxSize/2;
		
		double[][] toReturn=new double[size][2];

     	int counter=0;;
		for (Double value : this.keySet()) {
			toReturn[counter][0]=value;
     		toReturn[counter][1]=this.get(value);
     		counter++;
     		if (counter==size) break;
		}
		
    	return toReturn;
	}
	
	
	/** Returns a list of X/Y values where Y is bigger than threshold %
	 */
	public String toString(double threshold) {
		int size=this.size();
		if (size>(maxSize/2)) size=maxSize/2;
		
		String toReturn="";
		Iterator iterator=this.entrySet().iterator();
     	int counter=0;;
     	while (iterator.hasNext() && counter<size) {
     		counter++;
     		Map.Entry entry=(Map.Entry)iterator.next();
     		double x=((Double)entry.getKey()).doubleValue();
     		double y=((Double)entry.getValue()).doubleValue();
     		if (y>threshold) {
         		toReturn+=x+", "+y+"\r\n";
         	}
        }
        return toReturn;
	}

	/** Returns a list of X/Y values
	 */
	public String toString() {
		return toString(-1e100);
	}
	
	private String toFixed(double number) {
		BigDecimal big = new BigDecimal(number);
		String str = big.setScale(5, BigDecimal.ROUND_HALF_UP).toString();
		if(str.contains(".")) {
			return str.replaceAll("\\.?0+$", "");
		}
		return str;
	}
	
	public static void main(String args[]) {
		DoubleTreeMap treeMap=new DoubleTreeMap();
		treeMap.put(new Double(1),new Double(1));
		treeMap.put(new Double(1.1),new Double(2));
		treeMap.put(new Double(1.6),new Double(3));
		treeMap.put(new Double(2),new Double(0.1));
		treeMap.put(new Double(2.4),new Double(4));

		System.out.println("Original Map:\r\n"+treeMap.toString());
		
		treeMap.resize();
		
		System.out.println("Resize Map:\r\n"+treeMap.toString());
		
		treeMap.multiplyY(2);
		
		System.out.println("Map Y * 2:\r\n"+treeMap.toString());
		
		treeMap.addY(0.1);
		
		System.out.println("Map Y + 0.1:\r\n"+treeMap.toString());

		treeMap.multiplyX(2);
		
		System.out.println("Map X * 2:\r\n"+treeMap.toString());
		
		treeMap.addX(0.1);
		
		System.out.println("Map X + 0.1:\r\n"+treeMap.toString());

		treeMap.normalize();
		
		System.out.println("Normalize to 1:\r\n"+treeMap.toString());
		
		treeMap.cutoff(0.5);
		
		System.out.println("Cutoff to 0.5:\r\n"+treeMap.toString());
		
		
		DoubleTreeMap treeMapToAdd=new DoubleTreeMap();
		treeMapToAdd.put(new Double(1),new Double(1));
		treeMapToAdd.put(new Double(2),new Double(2));
		System.out.println("Adding map:\r\n"+treeMapToAdd.toString());
		
		treeMap.add(treeMapToAdd);
		System.out.println("Resulting map:\r\n"+treeMap.toString());
		
		System.out.println ("X of maxY is: "+treeMap.getXOfMaxY());
		
		
	}
	
}