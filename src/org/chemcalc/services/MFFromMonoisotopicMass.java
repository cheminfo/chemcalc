/* ChemCalc servlet
 * A chemical calculator
 * 
 * @author Michal Krompiec
 * @author Dr Luc Patiny
 *
 * @version 9.2, build 12 Dec 2003
 *
**/ 
package org.chemcalc.services;

import org.chemcalc.core.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

public class MFFromMonoisotopicMass {
    
	private final static int MAXIMUM_ITERATIONS=2000000000;
	private final static int DEFAULT_MAXIMUM_ROWS=1000;
	final static boolean DEBUG=false;
	final static boolean OPTIMIZE=true;
	final static double NUMBER_DIGITS=1e10;
	
    public static JSONObject execute(HashMap<String,String> options, HashMap<String,Group> groups, HashMap<String,Element> elements) {
    	double minMass=100;
    	double maxMass=200;
    	double massRange=0.5;
    	double minUnsaturation=0;
		double maxUnsaturation=50;
		double maxNumberRows=DEFAULT_MAXIMUM_ROWS;
		boolean integerUnsaturation=false;
		boolean useUnsaturation=false;
		boolean numberOfResultsOnly=false;
		boolean jcampLink=(options.get("jcampLink")==null)?false:Boolean.valueOf(options.get("jcampLink"));
		String jcampBaseURL=(options.get("jcampBaseURL")==null)?"http://www.chemcalc.org/service/jcamp/":options.get("jcampBaseURL");
		String mfRange="C0-100H0-202N0-10O0-10F0-3Cl0-3Br0-1";
    	double targetMonoisotopicMass=Double.MIN_VALUE;
    	TreeSet<Result> results=new TreeSet<Result> ();
    	boolean typedResult=(options.get("typedResult")==null)?false:Boolean.valueOf(options.get("typedResult"));	// should we have the type of the different information

    	Double fwhm=0.001;
    	try {
    		fwhm=Double.parseDouble((String)options.get("fwhm"));
    	} catch (Exception e) {}
    	
    	int gaussianWidth=0;
    	try {
    		gaussianWidth=(int)Double.parseDouble((String)options.get("gaussianWidth"));
    	} catch (Exception e) {
    	}

    	
    	
    	try {
	    	JSONObject json=new JSONObject();
	    	if (options==null) {
	    		json.put("error", "options is null");
	    		return json;
	    	}
	    	
	    	String referenceVersion=options.get("referenceVersion");
	    	if (referenceVersion!=null && ! referenceVersion.equals("")) {
	    		try {
	    			elements=LoadFromFiles.elements(referenceVersion);
	    		} catch (IOException e) {
	    			e.printStackTrace(System.out);
	    		}
	    	}
	    	
	    	Double defaultUnsaturationContribution=null;
	    	try {
	    		defaultUnsaturationContribution=Double.parseDouble((String)options.get("defaultUnsaturationContribution"));
	    	} catch (Exception e) {}
			try {minUnsaturation=Double.parseDouble(options.get("minUnsaturation"));} catch (Exception e) {}
			try {maxUnsaturation=Double.parseDouble(options.get("maxUnsaturation"));} catch (Exception e) {}
			if (options.containsKey("integerUnsaturation")) {
				try {integerUnsaturation=Boolean.parseBoolean(options.get("integerUnsaturation"));} catch (Exception e) {}
			}
			if (options.containsKey("useUnsaturation")) {
				try {useUnsaturation=Boolean.parseBoolean(options.get("useUnsaturation"));} catch (Exception e) {}
			}
			if (options.containsKey("numberOfResultsOnly")) {
				try {numberOfResultsOnly=Boolean.parseBoolean(options.get("numberOfResultsOnly"));} catch (Exception e) {}
			}

			try {maxNumberRows=Double.parseDouble(options.get("maxNumberRows"));} catch (Exception e) {}
			
			try {targetMonoisotopicMass=Double.parseDouble(options.get("monoisotopicMass"));} catch (Exception e) {}
			try {massRange=Double.parseDouble(options.get("massRange"));} catch (Exception e) {}
			minMass=targetMonoisotopicMass-massRange;
			maxMass=targetMonoisotopicMass+massRange;

			try {maxMass=Double.parseDouble(options.get("maxMass"));} catch (Exception e) {}
			if (options.containsKey("mfRange") && options.get("mfRange")!=null && options.get("mfRange").length()>0) {
				mfRange=options.get("mfRange");
			}
			
	    	if (targetMonoisotopicMass<0) {
	    		json.put("error", "monoisotopicMass must be specified and larger than 0");
	    		return json;
	    	}			
			
	    	JSONObject jsonOptions=new JSONObject();
	    	jsonOptions.put("minUnsaturation",minUnsaturation);
	    	jsonOptions.put("maxUnsaturation",maxUnsaturation);
	    	jsonOptions.put("integerUnsaturation",integerUnsaturation);
	    	jsonOptions.put("useUnsaturation",useUnsaturation);
	    	jsonOptions.put("numberOfResultsOnly",numberOfResultsOnly);
	    	jsonOptions.put("minMass",Math.round(minMass*NUMBER_DIGITS)/NUMBER_DIGITS);
	    	jsonOptions.put("maxMass",Math.round(maxMass*NUMBER_DIGITS)/NUMBER_DIGITS);
	    	jsonOptions.put("massRange",Math.round(massRange*NUMBER_DIGITS)/NUMBER_DIGITS);
	    	jsonOptions.put("mfRange",mfRange);
	    	jsonOptions.put("monoisotopicMass",Math.round(targetMonoisotopicMass*NUMBER_DIGITS)/NUMBER_DIGITS);
	    	jsonOptions.put("referenceVersion",referenceVersion);
	    	jsonOptions.put("jcampLink",jcampLink);
	    	jsonOptions.put("jcampBaseURL",jcampBaseURL);
	    	jsonOptions.put("typedResult",typedResult);
	    	
	    	json.put("options", jsonOptions);
	    	
	    	long iterationNumber=0;
	    	long numberResults=0;
	    	
	    	int charge=0;
	    	int chargeAbs=0;
	    	
			try {
				Formula allowedFormula=new Formula(mfRange,elements,groups,false);
				
				if (allowedFormula.getNumberOfParts()!=1) {
					json.put("error", "Number of parts not equal to 1");
		    		return json;
				}

				// We check if we have a charge. If yes, we need to take care about the electron and not forget to change everything when we print the results
				charge=allowedFormula.getParts().get(0).charge;
				chargeAbs=Math.abs(charge);
				if (charge!=0) {
					targetMonoisotopicMass=targetMonoisotopicMass*chargeAbs+charge*SpectrumCalculator.ELECTRON_MASS;
					minMass=minMass*chargeAbs+charge*SpectrumCalculator.ELECTRON_MASS;
					maxMass=maxMass*chargeAbs+charge*SpectrumCalculator.ELECTRON_MASS;
					massRange=massRange*chargeAbs;
				}
				
				Vector<ExtendedAtom> possibleAtoms=getPossibleAtoms(allowedFormula.getParts().get(0), targetMonoisotopicMass+massRange);
				
				if (possibleAtoms!=null) {
				
					// we maintain in an array the hierarchy to have permanently the exact mass
					double[] currentMonoisotopicMass=new double[possibleAtoms.size()];
					// we have an array of the min and max inner mass for a specific level
					double[] minInnerMass=new double[possibleAtoms.size()];
					double[] maxInnerMass=new double[possibleAtoms.size()];
					
					
					
					calculateMinMaxInnerMass(possibleAtoms, minInnerMass, maxInnerMass);
					
					json.put("bruteForceIteration",checkIterations(possibleAtoms));
					
					
					ExtendedAtom currentAtom;
					boolean theEnd=false;
					Double unsaturation=null;
					int currentPosition=0;
					// we need to determine for the current position what is the range to study
					int maxPosition=possibleAtoms.size();
					possibleAtoms.elementAt(0).setCurrentMinMax(0, targetMonoisotopicMass, minInnerMass[0], maxInnerMass[0], massRange);
					
					while (!theEnd) {
						if (iterationNumber > MAXIMUM_ITERATIONS) {
							throw new MFException("Iteration number is over the current maximum of: "+MAXIMUM_ITERATIONS);
						}
						
						// TODO: unsaturation calculation could be optimized
						if ((currentMonoisotopicMass[currentPosition]>=minMass) && (currentMonoisotopicMass[currentPosition]<=maxMass)) {
							unsaturation=Unsaturation.getUnsaturation(possibleAtoms, defaultUnsaturationContribution);
							if ((! useUnsaturation ) || (unsaturation==null) || ((unsaturation>=minUnsaturation) && (unsaturation<=maxUnsaturation) && (! integerUnsaturation || (unsaturation==(int)unsaturation.doubleValue())))) {
								numberResults++;
								if (!numberOfResultsOnly) {
									if (results.size()>=maxNumberRows) {
										if (Math.abs(results.last().error)>Math.abs(currentMonoisotopicMass[currentPosition]-targetMonoisotopicMass)) {
											results.pollLast();
											results.add(new Result(currentMonoisotopicMass[currentPosition]-targetMonoisotopicMass, currentMonoisotopicMass[currentPosition], unsaturation, getMF(possibleAtoms, groups)));									
										}
									} else {
										results.add(new Result(currentMonoisotopicMass[currentPosition]-targetMonoisotopicMass, currentMonoisotopicMass[currentPosition], unsaturation, getMF(possibleAtoms, groups)));									
									}
								}
							}
						}
						
						// we need to setup all the arrays if possible
						while (currentPosition<maxPosition && currentPosition>=0) {
							iterationNumber++;
							currentAtom=possibleAtoms.elementAt(currentPosition);
							if (DEBUG) {
								System.out.println(currentAtom.atom.getSymbol()+" - "+currentPosition+" - "+maxPosition+" - "+currentAtom.currentCount+" - "+currentAtom.currentMinCount+" - "+currentAtom.currentMaxCount);
							}
							if (currentAtom.currentCount<currentAtom.currentMaxCount) {
								currentAtom.currentCount++;
								if (currentPosition==0) {
									currentMonoisotopicMass[currentPosition]=currentAtom.atom.getMonoisotopicMass()*currentAtom.currentCount;
								} else {
									currentMonoisotopicMass[currentPosition]=currentMonoisotopicMass[currentPosition-1]+currentAtom.atom.getMonoisotopicMass()*currentAtom.currentCount;
								}
								
								if (currentPosition<(maxPosition-1)) {
									currentPosition++;
									possibleAtoms.elementAt(currentPosition).setCurrentMinMax(currentMonoisotopicMass[currentPosition-1], targetMonoisotopicMass, minInnerMass[currentPosition], maxInnerMass[currentPosition], massRange);
								} else {
									break;
								}
							} else {					
								currentPosition--;
							}
						}
						
						if (currentPosition<0) {
							theEnd=true;
						}
						
						if (DEBUG) {
							for (int i=0; i<currentMonoisotopicMass.length; i++) {
								System.out.print(currentMonoisotopicMass[i]+" - ");
							}
							System.out.println("");
						}
					}
				}
				json.put("realIteration",iterationNumber-1);
				json.put("numberResults",numberResults);
				json.put("charge", charge);
				
			} catch (MFException e) {
				json.put("error", e.toString()+"\r"+jsonOptions.toString());
			}
			
			
			
			JSONArray jsonResults=new JSONArray();
			
			String chargeStr="";
			if (charge!=0) {
	    		if (charge>0) {
	    			chargeStr="(+"+charge+")";
	    		} else {
	    			chargeStr="("+charge+")";
	    		}
			}
			
			double error=0;
			double em=0;
			for (Result result : results) {
				JSONObject jsonResult=new JSONObject();
				if (charge==0) {
					error=Math.round(result.error*NUMBER_DIGITS)/NUMBER_DIGITS;
					em=Math.round(result.monoisotopicMass*NUMBER_DIGITS)/NUMBER_DIGITS;
				} else {
					error=Math.round((result.error)/chargeAbs*NUMBER_DIGITS)/NUMBER_DIGITS;
					em=Math.round((result.monoisotopicMass-charge*SpectrumCalculator.ELECTRON_MASS)/chargeAbs*NUMBER_DIGITS)/NUMBER_DIGITS;
				}
				jsonResult.put("error", error);
				jsonResult.put("em",em);
				jsonResult.put("ppm", Math.round(error/em*(NUMBER_DIGITS))/(NUMBER_DIGITS)*1e6);
				jsonResult.put("info", result.mf+chargeStr);
				try {
					// need to add parameters related to jcamp generation
					
					String jcampURL=jcampBaseURL+result.mf+"&fwhm="+fwhm;
					if (gaussianWidth>0) {
						jcampURL+="&gaussianWidth="+gaussianWidth;
					}
					if (typedResult) {
						JSONObject mfJSON=new JSONObject();
						mfJSON.put("type","mf");
						mfJSON.put("value", new Formula(result.mf+chargeStr, elements, groups).toString());
						jsonResult.put("mf", mfJSON);
						if (jcampLink) {
							JSONObject mfJcamp=new JSONObject();
							mfJcamp.put("type","jcamp");
							mfJcamp.put("url", jcampURL);
							jsonResult.put("jcamp", mfJcamp);
						}
					} else {
						jsonResult.put("mf", new Formula(result.mf+chargeStr, elements, groups).toString());
						if (jcampLink) {
							jsonResult.put("jcampURL", jcampURL);
						}
					}
				} catch (MFException e1) {
					jsonResult.put("mf", e1.toString());
				}
				try {
					jsonResult.put("unsat", Math.round(result.unsaturation*NUMBER_DIGITS)/NUMBER_DIGITS);
				} catch (Exception e) {};
				jsonResults.put(jsonResult);
			}
			json.put("results", jsonResults);
	    	
	        return json;
    	} catch (JSONException e) {
    		return null;
    	}
	}

	private static Vector<ExtendedAtom> getPossibleAtoms(FormulaPart formulaPart, double maximalMonoisotopicMass) {
		if (DEBUG) System.out.println("Get possible atoms");
		Vector<ExtendedAtom> possibleAtoms=new Vector<ExtendedAtom>();
		try {
			for (Atom atom : formulaPart) {
				atom.maxCount=Math.min(atom.maxCount, ((int)(maximalMonoisotopicMass/atom.getMonoisotopicMass())));
				if (atom.minCount > atom.maxCount) return null; // no way to get a molecular formula
				possibleAtoms.add(new ExtendedAtom(atom));	
			}
			Collections.sort(possibleAtoms);
			if (DEBUG) {
				for (ExtendedAtom atom : possibleAtoms) {
					System.out.println(atom.toString());
				}
			}
		} catch (MFException e) {}
		return possibleAtoms;
	}
	
	private static void calculateMinMaxInnerMass(Vector<ExtendedAtom> possibleAtoms, double[] minInnerMass, double[] maxInnerMass) {
		if (DEBUG) System.out.println("Calculating minInnerMass and maxInnerMass");
		for (int i=0; i<possibleAtoms.size(); i++) {
			for (int j=i+1; j<possibleAtoms.size(); j++) {
				minInnerMass[i]=minInnerMass[i]+possibleAtoms.get(j).getMinMass();
				maxInnerMass[i]=maxInnerMass[i]+possibleAtoms.get(j).getMaxMass();
			}
			if (DEBUG) {
				System.out.println("i: "+i+" - minInnerMass: "+minInnerMass[i]+" - maxInnerMass: "+maxInnerMass[i]);
			}
		}
	}
	
	
	
	private static String getMF(Vector<ExtendedAtom> possibleAtoms, HashMap<String,Group> groups) {
		// TODO: find a way to sort the atoms ...
		String mf="";
		for(ExtendedAtom extendedAtom : possibleAtoms) {
			String label=extendedAtom.atom.getFullSymbol();
			if (groups.containsKey(label)) {
				// we should not use the name but just the symbol except if a specified range
				if (groups.get(label).getName().startsWith("{")) {
					label=groups.get(label).getName();
				} else {
					label=groups.get(label).getSymbol();
				}
			}
			if (extendedAtom.currentCount>1) {
				mf+=label+(extendedAtom.currentCount);
			} else if (extendedAtom.currentCount>0) {
				mf+=label;
			}
		}
		return mf;
	
	}
	
	private static BigInteger checkIterations (Vector<ExtendedAtom> possibleAtoms) {
		BigInteger iterations=BigInteger.valueOf(1);
		for(ExtendedAtom extendedAtom : possibleAtoms) {
			iterations=iterations.multiply(BigInteger.valueOf(extendedAtom.atom.maxCount-extendedAtom.atom.minCount+1));
		}
		return iterations;
	}
}



class Result implements Comparable<Result> {
	double error;
	double monoisotopicMass;
	Double unsaturation;
	String mf;

	public Result (double error, double monoisotopicMass, Double unsaturation, String mf)  {
		this.error=error;
		this.monoisotopicMass=monoisotopicMass;
		this.mf=mf;
		this.unsaturation=unsaturation;
	}
	
	public void setResult(double error, double monoisotopicMass, Double unsaturation, String mf)  {
		this.error=error;
		this.monoisotopicMass=monoisotopicMass;
		this.mf=mf;
		this.unsaturation=unsaturation;
	}
	
	
	public int compareTo(Result otherObject) {
		// it may not be 0 so that we never have an equality
		int result=Double.compare(Math.abs(error), Math.abs(otherObject.error));
		if (result!=0) return result;
		return mf.compareTo(otherObject.mf);
	}
}