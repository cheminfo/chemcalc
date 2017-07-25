package org.chemcalc.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.chemcalc.core.MFException;
import org.chemcalc.services.JSONForMF;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TestRange {

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
	public void Charge() throws MFException, JSONException {
		init();
		
		options.put("mf", "C1-10H20");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getDouble("charge"),0.0,0.00001);

		options.put("mf", "C1-10H20(2+)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getDouble("charge"),2.0,0.00001);
	}
	

	
}
