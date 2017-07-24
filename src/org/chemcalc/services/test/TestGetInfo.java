package org.chemcalc.services.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.chemcalc.core.MFException;
import org.chemcalc.services.GetInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class TestGetInfo {

	public static void main(String[] args) throws JSONException, MFException {
		 
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();
		HashMap<String,String> options=new HashMap<String,String>();

		try {
			elements = LoadFromFiles.elements("1995");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		options.put("referenceVersion", "2012"); // 1995 or 2012
		
		JSONObject json=GetInfo.execute(options,groups,elements);
		
		System.out.println(json.toString());
	}
	


	


}
