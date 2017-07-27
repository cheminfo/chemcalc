package org.chemcalc.test;

import org.chemcalc.core.*;
import org.chemcalc.services.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

public class Isotopomers {

	HashMap<String,Element> elements=new HashMap<String,Element>();
	HashMap<String,Group> groups=new HashMap<String,Group>();
	HashMap<String,String> options=new HashMap<String,String>();
	boolean init=false;

	private void init() throws MFException {
		options=new HashMap<String,String>();
		if (init) return;

		try {
			elements = LoadFromFiles.elements("data/atom201502.txt","data/isotope201502.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		init=true;
	}
	

	@Test
	public void isotopicDistribution() throws MFException, JSONException {
		init();
		
		options.put("mf", "C10Cl10");
		options.put("isotopomers", "xy");
		options.put("joiningAlgorithm", "center"); // main

		options.put("fwhm", "0.1");
		JSONObject json=JSONForMF.execute(options,groups,elements);

		
		String xy=json.getString("xy");

		System.out.println(xy);
		int index=xy.indexOf(", 100");
	//	Assert.assertEquals(xy.indexOf("12010735.897, 100"),11386);
	}
	
}
