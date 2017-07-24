package org.chemcalc.services.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.chemcalc.core.MFException;
import org.chemcalc.services.JSONForMF;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class TestJsonForMF201507 {

	public static void main(String[] args) throws JSONException, MFException {
		
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();
		HashMap<String,String> options=new HashMap<String,String>();

		try {
			elements = LoadFromFiles.elements("data/atom201507.txt","data/isotope201507.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		options.put("mf", "Tc");

		JSONObject json=JSONForMF.execute(options,groups,elements);

		 System.out.println(json.toString());

	}
	


}
