package org.chemcalc.services.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.MFException;
import org.chemcalc.services.MFRangeIntersection;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TestMFRangeIntersection {

	public static void main(String[] args) throws JSONException, MFException {
		
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();
		HashMap<String,String> options=new HashMap<String,String>();

		MFFromMonoisotopicMassTest.initRealElement(elements);
		MFFromMonoisotopicMassTest.initRealGroup(elements, groups);

		testSimple(elements, groups, options);
	}
	


	
	public static void testSimple(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mf1", "C10H8Cl");
		options.put("mf2", "C3-5H1-10Cl3");
		JSONObject json=MFRangeIntersection.execute(options,groups,elements);
		System.out.println(json.toString());
	}
	


}
