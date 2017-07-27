package org.chemcalc.core;

import org.chemcalc.core.util.AGaussian;
import org.chemcalc.core.util.Gaussian;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * Calculates the isotopic distribution spectrum.
 *
 * version 9.2 (Dec 22, 2003)
 *
 * NOTE: this implementation is not synchronized!
 *
 * @author Michal Krompiec, Dr Luc Patiny
 */
public class SpectrumCalculator {
/**
 * Calculates the isotopic distribution for the given formula.
 * @param elements chemical elements
 * @param mf the molecular formula
 * @param fwhm  peak width in the spectrum
 * @throws MFException if any error occured during the calculation
 */
	public final static double ELECTRON_MASS=5.4857990946e-4;
 	IsotopicDistribution isotopicDistribution=null;
    private double MASS_FWHM;
    private double INTENSITY_CUTOFF;
    private HashMap<String, Element> elements;
    private Formula formula;
    private HashMap<Double,String> labels=new HashMap<Double,String>();
    // maximal size of distribution
    public final static byte JOINING_MAIN_PEAK=0;
    public final static byte JOINING_CENTER_MASS=1;
    private byte joiningAlgorithm=JOINING_MAIN_PEAK;
    
    public SpectrumCalculator(HashMap<String, Element> elements, Formula formula, double fwhm) throws MFException {
    	this(elements, formula, fwhm, 1e-5);
    }

    
    public SpectrumCalculator(HashMap<String, Element> elements, Formula formula, double fwhm, double threshold) throws MFException {
	   this.elements=elements;
	   this.formula=formula;
	   this.MASS_FWHM=fwhm;
	   this.INTENSITY_CUTOFF=threshold;
    }
	
    public void setJoiningAlgorithm(byte joiningAlgorithm) {
    	this.joiningAlgorithm=joiningAlgorithm;
    }
    
	public DoubleTreeMap getIsotopicDistribution() {
		return isotopicDistribution.result;
	}
	
	public void calculate() throws MFException {
		// A formula may contain many parts
		boolean first=true;
		IsotopicDistribution currentDist;
		for (FormulaPart mf : formula.getParts()) {
			if (first) {
				isotopicDistribution=new IsotopicDistribution(mf, elements);
				isotopicDistribution.result.multiplyY(mf.getNumber());
				currentDist=isotopicDistribution;
				first=false;
			} else {
				currentDist=new IsotopicDistribution(mf, elements);
				currentDist.result.multiplyY(mf.getNumber());
				isotopicDistribution.result.add(currentDist.result);
			}
			
			if (mf.comment!="") {
				Double xForMaxY=currentDist.result.getXforMaxY();
				if (labels.containsKey(xForMaxY)) {
					labels.put(xForMaxY, labels.get(xForMaxY)+","+mf.comment);
				} else {
					labels.put(xForMaxY, mf.comment);
				}
			}
		}
		if (isotopicDistribution.result.size()==0) throw new MFException("Isotopic distribution: empty table");
    }

	public void normalize(double factor) throws MFException {
		isotopicDistribution.normalize(factor, MASS_FWHM, INTENSITY_CUTOFF);
	}

	public void normalize() throws MFException {
		double factor=Double.MIN_VALUE;
		for (FormulaPart mf : formula.getParts()) {
			if (mf.getNumber()>factor) {
				factor=mf.getNumber();
			}
		}
		this.normalize(factor);
	}

  
 	public StringBuffer toJcamp() {
     	if (isotopicDistribution.result.size()==0) throw new RuntimeException("No isotopic distribution data");
     	// we will add a 0 value at the beginning and the end so that is looks nice ...
     	isotopicDistribution.result.put(new Double(isotopicDistribution.result.getMinX().doubleValue()-2),new Double(0));
     	isotopicDistribution.result.put(new Double(isotopicDistribution.result.getMaxX().doubleValue()+2),new Double(0));
     	
     	HashMap<String,String> extraInfo=new HashMap<String,String>();
     	extraInfo.put("FWHM", MASS_FWHM+"");
     	extraInfo.put("Threshold", INTENSITY_CUTOFF+"");
     	try {
	     	if (this.formula.getNumberOfParts()==1) {
	     		FormulaPart fp=formula.getPart(1);
	     		extraInfo.put("Monoisotopic mass", fp.getMonoisotopicMass()+"");
	     		extraInfo.put("Molecular weight", fp.getMass()+"");
	     		if (Unsaturation.getUnsaturation(fp, null)!=null) {
	     			extraInfo.put("Unsaturation", Unsaturation.getUnsaturation(fp, null)+"");
	     		}
	     	}
     	} catch (Exception e) {
     		e.printStackTrace(System.out);
     	}
     	return isotopicDistribution.result.toMassJcamp(formula+" - fwhm: "+MASS_FWHM,labels,extraInfo);
	}

	public void applyGaussian(AGaussian gaussian, double finalFWHM) {
		double[][] result=Gaussian.combineGaussian(this.toXYArray(), gaussian, finalFWHM);
		DoubleTreeMap newResult=new DoubleTreeMap();
		for (int i=0; i<result.length; i++) {
			newResult.put(result[i][0],result[i][1]);
		}
		isotopicDistribution.result=newResult;
		isotopicDistribution.result.setMaxSize(1000000);
	}
	
	
 	public StringBuffer toXY() {
     	if (isotopicDistribution.result.size()==0) throw new RuntimeException("No isotopic distribution data");
     	return isotopicDistribution.result.toXY();
	}
	
 	public double[][] toXYArray() {
     	if (isotopicDistribution.result.size()==0) throw new RuntimeException("No isotopic distribution data");
     	return isotopicDistribution.result.toXYArray();
	}
 	
	class IsotopicDistribution {
	/**
  	 * The minimal relative abundance to be considered non-zero
  	 */
	//    final static private double INTENSITY_CUTOFF=1e-10; // minimal percentage to consider
	    private double massFWHM=MASS_FWHM;
	 	DoubleTreeMap result=new DoubleTreeMap();
	 	HashMap<String,Element> elements;
	 	
	 	private IsotopicDistribution(Element element, HashMap<String,Element> elements) throws MFException {
	    	this.elements=elements;
	    	int howMany=element.getNumIsotopes();
	    	setFWHM(MASS_FWHM/(howMany*4));
	    	
	    	for (Isotope isotope : element.getIsotopes().values()) {
	    		result.put(new Double(isotope.mass), new Double(isotope.percentage/100));
	    	}
	    }
	    
	    IsotopicDistribution(FormulaPart formulaPart, HashMap<String,Element> elements) throws MFException {
	    	this.elements=elements;
	    	calculateDistribution(formulaPart);
		    if (formulaPart.charge!=0) {
		    	// we need to remove / add the mass of the electron
		    	result.addX(-formulaPart.charge*ELECTRON_MASS);
		    	// we need to divide by the charge
		    	result.divideX(Math.abs(formulaPart.charge));
		    }
	    }
		
	 	private IsotopicDistribution(DoubleTreeMap d) {
	 		result=d;
	 	}
	 	
	    
		void setFWHM(double fwhm) {
			if (fwhm<1e-10) fwhm=1e-10;
			this.massFWHM=fwhm;
		}

	/**
	 * Calculates isotopic distribution
	 */
		 private void calculateDistribution(FormulaPart formulaPart) throws MFException{
			 // there is a possibility to improve the performance by changing the order in which we put the atoms
			 
			 FormulaPart sortedFormulaPart = new FormulaPart(new Comparator<Atom>() {
		            public int compare(Atom atom1, Atom atom2) {
		            	double complexity1=atom1.getIsotopeNumber()!=0 ? atom1.getCount() : Math.pow(elements.get(atom1.getSymbol()).getNumIsotopes()-1, atom1.getCount());
		            	double complexity2=atom2.getIsotopeNumber()!=0 ? atom2.getCount() : Math.pow(elements.get(atom2.getSymbol()).getNumIsotopes()-1, atom2.getCount());
		     //       	System.out.println(elements.get(atom1.getSymbol()).getNumIsotopes()+" - "+complexity1+" "+complexity2);
		            	if (complexity1==complexity2) return Double.compare(atom1.getMass(),atom2.getMass());
		            	return Double.compare(complexity1,complexity2)*-1;
		            }
		        }
			 );
			 sortedFormulaPart.addAll(formulaPart);
			 
			// System.out.println(sortedFormulaPart);
			 
		 	int maxIsot=1;
		 	for (Atom atom : sortedFormulaPart) {
		 		Element elm=(Element)elements.get(atom.getSymbol());
		 		if (elm.getNumIsotopes()>maxIsot)
		 			maxIsot=elm.getNumIsotopes();
		 	}
		 	// we need to add 1 otherwise if there is only one atom the log is 0 and we need an infinite resolution
		 	double numAdds=2*maxIsot*Math.log(sortedFormulaPart.getNumberOfAtoms()+1)/Math.log(2);
		 	double res=MASS_FWHM/(numAdds*4);
		 	setFWHM(res);
		 	result.put(new Double(0), new Double(1));

		  	// we should order the molecular formula based on the number of isotopes! This would speed up the calculation		  	
		  	for (Atom atom : sortedFormulaPart) {
				Element elm=(Element)elements.get(atom.getSymbol());
		   		if (atom.getIsotopeNumber()!=0) { 
		   			// a specific isotope: define a virtual element
		       		Element elem=new Element(atom.getMass(),atom.getMass(),elm.getSymbol(),elm.getName(),elm.getAtomicNumber());
		       		elm=elem;
		       	}
		   		if (elm.getNumIsotopes()==0) {
		   			//one isotope only, specify it
		   			Isotope is=new Isotope((int)(elm.getMass()+.5),elm.getMass(), 1, elm.getSymbol());
		      		elm.addIsotope(is); 
		     	}
		   		
		   		if (atom.getCount()<0) {
		   			throw new MFException("Number of element under 0");
		   		} else if (atom.getCount()==0) {
		   			
		   		} else if (atom.getCount()==1) {//one atom only
		   			addAtom(elm);
		   		} else { //more than one
		   			//convert this element to isotopic distribution
		   			IsotopicDistribution elementDistribution=new IsotopicDistribution(elm, elements); 
		   			//set the final mass FWHM
		   			elementDistribution.setFWHM(this.massFWHM);
		   			
		   			//for n atoms, calculate nth power
		   			//and multiply the current distribution with the result
		   			result=multiplyWith(elementDistribution.calcPower(atom.getCount()));
		   		}
	   		
		  	}// end while
		  		
	 	}

	 	IsotopicDistribution calcPower(int p) throws MFException {
	 		if (p<=0) throw new RuntimeException("Error: power="+p);
	 		if (p==1) return this;
	 		if (p==2) {
	 			result=calcSquare();
	 			return this;
	 		}
	 		p--;
	 		IsotopicDistribution base=new IsotopicDistribution(result); //linear time
	 		//loop: log2(p) * multiplyWith(base) * 2
	 		while (p!=0) {
	 			if ((p&1)!=0) {
	 				result=multiplyWith(base); //executed <= log2(p) times
	 			}
	 			p>>=1;
	 			if (p!=0) base.result=base.calcSquare();//executed <= log2(p) times
	 		}
	 		return this;
		}

	 	DoubleTreeMap calcSquare() throws MFException {
	 		return multiplyWith(this);
	 	}


	 	private DoubleTreeMap multiplyWith(IsotopicDistribution isotopicDistribution) throws MFException {
	 		if (joiningAlgorithm==JOINING_MAIN_PEAK) {
	 			return multiplyWithV1(isotopicDistribution);
	 		} else if (joiningAlgorithm==JOINING_CENTER_MASS) {
	 			return multiplyWithV2(isotopicDistribution);
	 		}
	 		
	 		return multiplyWithV1(isotopicDistribution);	 			
	 	}
	 	
	 	private DoubleTreeMap multiplyWithV1(IsotopicDistribution isotopicDistribution) throws MFException {
	 		// System.out.println("V1");
	 		int maxSize=isotopicDistribution.result.getMaxSize();
	 		if (this.result.size()>maxSize) {this.result.resize();}
	 		if (isotopicDistribution.result.size()>maxSize) {isotopicDistribution.result.resize();}
	 		
 			DoubleTreeMap newDist=new DoubleTreeMap();
 			Iterator multPeaks=isotopicDistribution.result.entrySet().iterator();
 			double maxIntens=0;
 			Map.Entry multPeak;
 			Double multMass;
 			Double multIntens;
 			Double floorMass, ceilingMass;
 			Iterator oldPeaks;
 			Map.Entry oldPeak;
 			Double oldMass;
 			Double oldIntens;
 			while (multPeaks.hasNext()) {
 				multPeak=(Map.Entry)multPeaks.next();
 				multMass=(Double)multPeak.getKey();
 				multIntens=(Double)multPeak.getValue();
 				oldPeaks=result.entrySet().iterator();
 				while (oldPeaks.hasNext()) {
 					oldPeak=(Map.Entry)oldPeaks.next();
 					oldMass=(Double)oldPeak.getKey();
 					oldIntens=(Double)oldPeak.getValue();
 					double newMass=oldMass.doubleValue()+multMass.doubleValue();
 					double newIntens=oldIntens.doubleValue()*multIntens.doubleValue();
 					
 					// if we round the newMass we will add a lot of errors !
 					// newMass=Math.rint(newMass/massResolution)*massResolution;
 					
 					// We will just check if there is a mass that is close to the target ...
 					// in this case we will check which one is the biggest and it will be the reference
 					floorMass=newDist.floorKey(newMass);
 					ceilingMass=newDist.ceilingKey(newMass);
 					
 					if (floorMass!=null && floorMass.equals(newMass)) { // same mass ...
 						newIntens+=newDist.get(ceilingMass);
 					} else {
 					
	 					// we first determine if we need to take care about the existing peaks
	 					if ((floorMass!=null)  && (Math.abs(floorMass-newMass)>massFWHM)) {
	 						floorMass=null;
	 					}
	 					if ((ceilingMass!=null) &&  (Math.abs(ceilingMass-newMass)>massFWHM)) {
	 						ceilingMass=null;
	 					}
	 					if ((ceilingMass!=null) && (floorMass!=null)) {
	 						if (Math.abs(ceilingMass-newMass)<Math.abs(floorMass-newMass)) {
	 							floorMass=null;
	 						} else {
	 							ceilingMass=null;
	 						}
	 					}
	 					
	 					if (ceilingMass!=null) {
							if (newDist.get(ceilingMass)>newIntens) {
								newIntens+=newDist.get(ceilingMass);
								newMass=ceilingMass;
							} else {
								newIntens+=newDist.get(ceilingMass);
								newDist.remove(ceilingMass);
							}
	 					} else if (floorMass!=null) {
							if (newDist.get(floorMass)>newIntens) {
								newIntens+=newDist.get(floorMass);
								newMass=floorMass;
							} else {
								newIntens+=newDist.get(floorMass);
								newDist.remove(floorMass);
							}
	 					}
 					}
 					
					if (newIntens>(INTENSITY_CUTOFF/1e5)) {
						newDist.put(new Double(newMass),new Double(newIntens));
					}			
 					
 					if (newIntens>maxIntens) maxIntens=newIntens;
 				} //end for each old peak
				if (newDist.size()>maxSize) {
					newDist.resize();
					// throw new MFException("Calculation of isotopic distribution too complex");
				}
 			} //end for each mult peak
 			
 			//normalisation  LP: Is this necessary ?
 			newDist.divideY(maxIntens);
 			return newDist;
 		}
	 	
	 	private DoubleTreeMap multiplyWithV2(IsotopicDistribution isotopicDistribution) throws MFException {
	 		// System.out.println("V2");
	 		int maxSize=isotopicDistribution.result.getMaxSize();
	 		if (this.result.size()>maxSize) {this.result.resize();}
	 		if (isotopicDistribution.result.size()>maxSize) {isotopicDistribution.result.resize();}
	 		
 			DoubleTreeMap newDist=new DoubleTreeMap();
 			Iterator multPeaks=isotopicDistribution.result.entrySet().iterator();
 			double maxIntens=0;
 			Map.Entry multPeak;
 			Double multMass;
 			Double multIntens;
 			Double floorMass, ceilingMass;
 			Iterator oldPeaks;
 			Map.Entry oldPeak;
 			Double oldMass;
 			Double oldIntens;
 			

 			
 			while (multPeaks.hasNext()) {
 				multPeak=(Map.Entry)multPeaks.next();
 				multMass=(Double)multPeak.getKey();
 				multIntens=(Double)multPeak.getValue();
 				oldPeaks=result.entrySet().iterator();
 				while (oldPeaks.hasNext()) {
 					oldPeak=(Map.Entry)oldPeaks.next();
 					oldMass=(Double)oldPeak.getKey();
 					oldIntens=(Double)oldPeak.getValue();
 					double newMass=oldMass.doubleValue()+multMass.doubleValue();
 					double newIntens=oldIntens.doubleValue()*multIntens.doubleValue();
 					
 					// if we round the newMass we will add a lot of errors !
 					// newMass=Math.rint(newMass/massResolution)*massResolution;
 					
 					// We will just check if there is a mass that is close to the target ...
 					// in this case we will check which one is the biggest and it will be the reference
 					floorMass=newDist.floorKey(newMass);
 					ceilingMass=newDist.ceilingKey(newMass);
 					
 					if (floorMass!=null && floorMass.equals(newMass)) { // same mass ...
 						newIntens+=newDist.get(ceilingMass);
 					} else {
 					
	 					// we first determine if we need to take care about the existing peaks
	 					if ((floorMass!=null)  && (Math.abs(floorMass-newMass)>massFWHM)) {
	 						floorMass=null;
	 					}
	 					if ((ceilingMass!=null) &&  (Math.abs(ceilingMass-newMass)>massFWHM)) {
	 						ceilingMass=null;
	 					}
	 					
	 					// we have 3 cases
	 					if ((ceilingMass!=null) && (floorMass!=null)) { // we average both peaks
	 						newMass=(newMass*newIntens+ceilingMass*newDist.get(ceilingMass)+floorMass*newDist.get(floorMass))/(newIntens+newDist.get(ceilingMass)+newDist.get(floorMass));
	 						newIntens+=newDist.get(ceilingMass)+newDist.get(floorMass);
	 						// System.out.println("join 2 peaks: "+newMass+"("+newIntens+")  "+ceilingMass+"("+newDist.get(ceilingMass)+") - "+floorMass+"("+newDist.get(floorMass)+")");
	 						newDist.remove(ceilingMass);
							newDist.remove(floorMass);
	 					} else if (ceilingMass!=null) {
	 						newMass=(newMass*newIntens+ceilingMass*newDist.get(ceilingMass))/(newIntens+newDist.get(ceilingMass));
	 						newIntens+=newDist.get(ceilingMass);
							newDist.remove(ceilingMass);
	 					} else if (floorMass!=null) {
	 						newMass=(newMass*newIntens+floorMass*newDist.get(floorMass))/(newIntens+newDist.get(floorMass));
	 						newIntens+=newDist.get(floorMass);
							newDist.remove(floorMass);
	 					}
 					}
 					
					if (newIntens>(INTENSITY_CUTOFF/1e5)) {
						// System.out.println(ceilingMass+" - "+floorMass+" - "+"Add intensity: "+newMass+"("+newIntens+")");
						newDist.put(new Double(newMass),new Double(newIntens));
					}			
 					
 					if (newIntens>maxIntens) maxIntens=newIntens;
 				} //end for each old peak
				if (newDist.size()>maxSize) {
					
					

				//	System.out.println(newDist.size());
				//	System.out.println("RESIZE");
					newDist.resize();
					// throw new MFException("Calculation of isotopic distribution too complex");
				}
 			} //end for each mult peak
 			
 			// System.out.println(newDist.toXY());
 			
 			//normalisation  LP: Is this necessary ?
 			newDist.divideY(maxIntens);
 			return newDist;
 		}
	 	
	 	
	 	private void addAtom(Element elm) throws MFException {
	 		result=multiplyWith(new IsotopicDistribution(elm, elements));
	 	}
	 	
		private void normalize(double factor, double fwhm, double intensityCutoff) {
			result.cutoff(intensityCutoff);
			switch (joiningAlgorithm) {
				case SpectrumCalculator.JOINING_MAIN_PEAK:
					result.combine(fwhm);
					break;
				case SpectrumCalculator.JOINING_CENTER_MASS:
					result.combineProportional(fwhm);
			}
			result.normalize(factor);
	    }
	} //end class Distribution
}