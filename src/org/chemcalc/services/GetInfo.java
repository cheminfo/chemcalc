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

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class GetInfo {
    

	
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
	    
			if (elements!=null) {
				JSONArray jsonElements=new JSONArray();
				json.put("elements", jsonElements);
				for (Element element : elements.values()) {
					jsonElements.put(element.toJSON());
				}
			}

			if (groups!=null) {
				JSONArray jsonGroups=new JSONArray();
				json.put("groups", jsonGroups);
				for (Group group : groups.values()) {
					jsonGroups.put(group.toJSON());
				}
			}
			
	        return json;
    	} catch (JSONException e) {
    		return null;
    	}
	}

    
}
