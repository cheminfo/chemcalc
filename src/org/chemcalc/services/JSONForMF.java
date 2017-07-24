/* ChemCalc a molecular formula calculator
 * 
 * @author Michal Krompiec
 * @author Dr Luc Patiny
 *
 * @version 9.2, build 12 Dec 2003
 *
**/ 
package org.chemcalc.services;

import org.chemcalc.core.*;
import org.chemcalc.core.util.AGaussian;
import org.chemcalc.core.util.Gaussian;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class JSONForMF {

	/**
	 * 
	 * @param options
	 * @param groups
	 * @param elements
	 * @return json
	 * 
	 * options:
	 *  - mf: mandatory: contains the molecular formula
	 *  - resolution: by default 0.001
	 *  - isotopomers: xy or jcamp (may contain both), array (compatibility reason), arrayXYXY or arrayXXYY
	 */
	
    public static JSONObject execute(HashMap<String,String> options, HashMap<String,Group> groups, HashMap<String,Element> elements) {
    	// this method is used in WmData and should stay public

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
	    	
	    	String mf=options.get("mf");
	    	String isotopomers=options.get("isotopomers");
	    	boolean typedResult=(options.get("typedResult")==null)?false:Boolean.valueOf(options.get("typedResult"));	// should we have the type of the different information

	    	Double fwhm=0.001;
	    	try {
	    		fwhm=Double.parseDouble((String)options.get("fwhm"));
	    	} catch (Exception e) {}
	    	
	    	Double threshold=1e-5;
	    	try {
	    		threshold=Double.parseDouble((String)options.get("threshold"));
	    	} catch (Exception e) {}
	    	
	    	int gaussianWidth=0;
	    	try {
	    		gaussianWidth=(int)Double.parseDouble((String)options.get("gaussianWidth"));
	    	} catch (Exception e) {
	    	}
	    	
	    	Double defaultUnsaturationContribution=null;
	    	try {
	    		defaultUnsaturationContribution=Double.parseDouble((String)options.get("defaultUnsaturationContribution"));
	    	} catch (Exception e) {}
	    	
	    	
	    	byte joiningAlgorithm=SpectrumCalculator.JOINING_MAIN_PEAK;
	    	if (options.containsKey("joiningAlgorithm")) {
	    		if (((String)options.get("joiningAlgorithm")).toLowerCase().equals("center")) {
	    			joiningAlgorithm=SpectrumCalculator.JOINING_CENTER_MASS;
	    		}
	    	}
	    	
	    	
	    	
	    	JSONObject jsonOptions=new JSONObject();
	    	jsonOptions.put("mf",mf);
	    	jsonOptions.put("threshold",threshold);
	    	jsonOptions.put("fwhm",fwhm);
	    	jsonOptions.put("typedResult",typedResult);
	    	jsonOptions.put("isotopomers",isotopomers);
	    	if (gaussianWidth>0) {
		    	jsonOptions.put("gaussianWidth",gaussianWidth);
	    	}
	    	jsonOptions.put("referenceVersion",referenceVersion);
	    	
	    	json.put("options", jsonOptions);
	    	
	        Formula formula=null;
	        if (mf!=null) {
	        	mf=mf.trim(); 
	        	if (mf.equals("")) mf=null;
	        }
	        if (mf != null) {
	        	try {
	        		formula=new Formula(mf,elements,groups);
					if (formula.getNumberOfParts()>0) { // we will give back an array ?
						if (formula.isUndefined()) {
							json.put("error", "mass not defined");
						} else {
							if (typedResult) {
								JSONObject mfJSON=new JSONObject();
								mfJSON.put("type","mf");
								mfJSON.put("value", formula.toString());
								json.put("mf", mfJSON);
							} else {
								json.put("mf", formula.toString());
							}
							json.put("charge", formula.getCharge());
							if (formula.isRange()==true) {
								json.put("isRange", true);
							} else {
								json.put("mw", formula.getMass());
								json.put("em", formula.getMonoisotopicMass());
								json.put("nominalMass", formula.getNominalMass());
								json.put("charge", formula.getCharge());
								try {
									json.put("unsaturation", formula.getUnsaturation(defaultUnsaturationContribution));
								} catch (Exception e) {}
								
								
								JSONArray globalEA=new JSONArray();
								for (Atom atom : formula.calculateElementalAnalysis()) {
									JSONObject eaPart=new JSONObject();
									eaPart.put("element", atom.getSymbol());
									eaPart.put("number", atom.getCount());
									eaPart.put("percentage", atom.getPercentage());
									globalEA.put(eaPart);
								}
								json.put("ea", globalEA);
								
		
								if ((isotopomers!=null) && (! isotopomers.equals(""))) {
						      		SpectrumCalculator spectrumCalculator=new SpectrumCalculator(elements,formula,fwhm,threshold);
						      		spectrumCalculator.setJoiningAlgorithm(joiningAlgorithm);
						     		spectrumCalculator.calculate();
							   		spectrumCalculator.normalize();
	
									if (gaussianWidth>0) {
										AGaussian gaussian=Gaussian.getGaussian(61, gaussianWidth);
										spectrumCalculator.applyGaussian(gaussian, fwhm);
									}
							   		
							   		jsonOptions.put("numberLines",spectrumCalculator.getIsotopicDistribution().size());
							   		
							   		if (isotopomers.indexOf("jcamp")>-1) {
							   			if (typedResult) {
								   			JSONObject jcampJSON=new JSONObject();
								   			jcampJSON.put("type","jcamp");
								   			jcampJSON.put("value", spectrumCalculator.toJcamp().toString());
								   			json.put("jcamp", jcampJSON);
							   			} else {
							   				json.put("jcamp", spectrumCalculator.toJcamp().toString());
							   			}
							   		}	   		
							   		if (isotopomers.indexOf("xy")>-1) {
							   			if (typedResult) {
								   			JSONObject xyJSON=new JSONObject();
								   			xyJSON.put("type","xy");
								   			xyJSON.put("value", spectrumCalculator.toXY().toString());
								   			json.put("xy", xyJSON);
							   			} else {
							   				json.put("xy", spectrumCalculator.toXY().toString());
							   			}
							   		}
							   		if (isotopomers.indexOf("array")>-1 && isotopomers.indexOf("arrayX")==-1 ) { // compatibility reason
							   			double [][] array=spectrumCalculator.toXYArray();
							   			JSONArray jsonArray=new JSONArray();
							   			JSONArray jsonX=new JSONArray();
							   			JSONArray jsonY=new JSONArray();
							   			for (int i=0; i<array.length; i++) {
							   				jsonX.put(array[i][0]);
							   				jsonY.put(array[i][1]);
							   			}
							   			jsonArray.put(jsonX);
							   			jsonArray.put(jsonY);
							   			json.put("spectrum", jsonArray);
							   		}
							   		if (isotopomers.indexOf("arrayXXYY")>-1) {
							   			double [][] array=spectrumCalculator.toXYArray();
							   			JSONArray jsonArray=new JSONArray();
							   			JSONArray jsonX=new JSONArray();
							   			JSONArray jsonY=new JSONArray();
							   			for (int i=0; i<array.length; i++) {
							   				jsonX.put(array[i][0]);
							   				jsonY.put(array[i][1]);
							   			}
							   			jsonArray.put(jsonX);
							   			jsonArray.put(jsonY);
							   			json.put("arrayXXYY", jsonArray);
							   		}
							   		if (isotopomers.indexOf("arrayXYXY")>-1) {
							   			double [][] array=spectrumCalculator.toXYArray();
							   			json.put("arrayXYXY", array);
							   		}
							   		
								}
								
								JSONArray parts=new JSONArray();
								json.put("parts",parts);
								
								for (FormulaPart formulaPart : formula.getParts()) {
									JSONObject part=new JSONObject();
									parts.put(part);
									if (typedResult) {
										JSONObject mfJSON=new JSONObject();
										mfJSON.put("type","mf");
										mfJSON.put("value", formulaPart.toString());
										part.put("mf", mfJSON);
									} else {
										part.put("mf", formulaPart.toString());
									}
									part.put("mw", formulaPart.getMass());
									part.put("em", formulaPart.getMonoisotopicMass());
									part.put("number", formulaPart.getNumber());
									if (formulaPart.comment.length()>0) part.put("comment", formulaPart.comment);
									part.put("unsaturation", Unsaturation.getUnsaturation(formulaPart,defaultUnsaturationContribution));
									double msem=formulaPart.getMonoisotopicMass()-formulaPart.charge*SpectrumCalculator.ELECTRON_MASS;
									if (formulaPart.charge!=0) {
										part.put("charge", formulaPart.charge);
										msem=msem/Math.abs(formulaPart.charge);
									}
									part.put("msem", msem);
									
									// We will add the elemental analysis of each part
									formulaPart.calculateElementalAnalysis();
									JSONArray ea=new JSONArray();
									for (Atom atom : formulaPart) {
										JSONObject eaPart=new JSONObject();
										eaPart.put("element", atom.getSymbol());
										eaPart.put("number", atom.getCount());
										eaPart.put("percentage", atom.getPercentage());
										ea.put(eaPart);
									}
									part.put("ea", ea);
								}
							}
						}
					} else { //no part in the molecule
						json.put("error", "there is no part in the molecule");
					}
				} catch (MFException mfe) {
					json.put("error", mfe.getMessage());
				}
			} else {
				json.put("error", "parameter mf not specified");
			}
	        return json;
    	} catch (JSONException e) {
    		return null;
    	}
	}

    
}
