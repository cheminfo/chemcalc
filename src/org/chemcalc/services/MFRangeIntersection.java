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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MFRangeIntersection {
    
	/**
	 * 
	 * @param mf1
	 * @param mf2
	 * @return json
	 * 
	 * options:
	 *  - mf1, mf2: mandatory: contains the molecular formula (that could be a range)
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
	    	
	    	String mfString1=options.get("mf1");
	    	String mfString2=options.get("mf2");
	    	boolean typedResult=(options.get("typedResult")==null)?false:Boolean.valueOf(options.get("typedResult"));	// should we have the type of the different information

	        if (mfString1!=null && mfString2!=null) {
	        	try {
	        		Formula formula1=new Formula(mfString1,elements,groups);
	        		Formula formula2=new Formula(mfString2,elements,groups);
					
	        		if (formula1.getNumberOfParts()==1 && formula2.getNumberOfParts()==1) {
	        			FormulaPart mf1=formula1.getPart(1);
	        			FormulaPart mf2=formula2.getPart(1);
	        			
	        			if (! formula1.isRange()) {
	        				for (Atom atom : mf1) {
	        					atom.minCount=0;
	        				}
	        			}
	        			if (! formula2.isRange()) {
	        				for (Atom atom : mf2) {
	        					atom.minCount=0;
	        				}
	        			}
	        			
	        			String newMF="";
	        			

	        			for (Atom atom : mf1) {
	        				if (mf2.contains(atom)) {
	        					int min=atom.minCount;
		        				int max=atom.maxCount;
		        				Atom otherAtom=mf2.findAtom(atom);
		        				if (otherAtom.minCount>min) min=otherAtom.minCount;
		        				if (otherAtom.maxCount<max) max=otherAtom.maxCount;
		        				
		        				if (min<=max) {
		        					newMF+=atom.getFullSymbol();
		        					if (min==max) {
		        						newMF+=min;
		        					} else {
		        						newMF+=min+"-"+max;
		        					}
		        				} else {
		        					newMF="";
		        					break;
		        				}
	        				} else {
	        					if (atom.minCount!=0) {
	        						newMF="";
		        					break;
	        					}
	        				}
	        			}
	        			for (Atom atom : mf2) {
	        				if (atom.minCount>0) {
		        				if (!mf1.contains(atom)) {
		        					newMF="";
		        					break;
		        				}
	        				}
	        			}
	        			
	        			if (typedResult) {
							JSONObject mfJSON=new JSONObject();
							mfJSON.put("type","mf");
							mfJSON.put("value", newMF);
							json.put("mf", mfJSON);
						} else {
							json.put("mf", newMF);
						}
					} else { //no part in the molecule
						json.put("error", "the molecular formula should exactly contain 1 part");
					}
				} catch (MFException mfe) {
					json.put("error", mfe.getMessage());
				}
			} else {
				json.put("error", "parameter mf1 or mf2 not specified");
			}
	        return json;
    	} catch (JSONException e) {
    		return null;
    	}
	}

    
}
