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

public class TestJsonForMF {

	public static void main(String[] args) throws JSONException, MFException {
		
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();
		HashMap<String,String> options=new HashMap<String,String>();

		try {
			elements = LoadFromFiles.elements("data/atom1995.txt","data/isotope1995.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		oneTest(elements, groups, options);
//		listAA(elements, groups, options);
	}
	

	public static void oneTest(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		long start=System.currentTimeMillis();
		JSONObject json=null;
		for (int i=0; i<10; i++) {
			options.put("mf", "(HAspAlaGluPheArg10HisAspSerGlyTyrGluValHisHisGlnLysOHRu2)");
			options.put("mf", "Ru10C1000H1000N1000");
			options.put("mf", "Ru10Cl10B10");
			options.put("mf", "C2");
		//	options.put("resolution", "0.001");
			options.put("isotopomers", "array");
			//options.put("gaussianWidth", "10");
			//options.put("gaussianResolution", "0.01");
		//	options.put("joiningAlgorithm","center");
			json=JSONForMF.execute(options,groups,elements);
			
		//	assertEquals(json.getDouble("em"),2038.08696);
		//	assertEquals(json.getDouble("mw"),2021.4);
		//	assertEquals(json.getString("mf"),"Ru2");
		}
		System.out.println("Time: "+(System.currentTimeMillis()-start));
		
		System.out.println(json.toString());
	}
	
	
	public static void testSimple(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		// options.put("mf", "C254 H378 N65 O75 S6");
		options.put("mf", "");
		options.put("referenceVersion","1995");
		options.put("resolution", "0.0001");
		options.put("isotopomers", "xy jcamp array");
		options.put("typedResult","true");
		options.put("threshold", "0.00001");

		
		
		JSONObject json=JSONForMF.execute(options,groups,elements);
		System.out.println(json.toString());
	}
	
	public static void listAA(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		String[] aas={"Ala","Arg","Asn","Asp","Cys","Gln","Glu","Gly","His","Ile","Leu","Lys","Met","Phe","Pro","Ser","Thr","Trp","Tyr","Val","H2O"};
		
		for (String aa : aas) {
			options.put("mf", aa);
			options.put("isotopomer", "false");
			options.put("resolution", "0.001");
			JSONObject json=JSONForMF.execute(options,groups,elements);
			System.out.println(aa+" - "+json.getJSONArray("parts").getJSONObject(0).getDouble("msem"));
		}
		
	}
}
