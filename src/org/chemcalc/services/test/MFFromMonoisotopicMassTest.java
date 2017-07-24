package org.chemcalc.services.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.chemcalc.core.MFException;
import org.chemcalc.services.MFFromMonoisotopicMass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MFFromMonoisotopicMassTest {

	public static void main(String[] args) throws JSONException, MFException {
		
	//	HashMap<String,Element> elements=new HashMap<String,Element>();
//		HashMap<String,Group> groups=new HashMap<String,Group>();
	//	initRealElement(elements);
	//	initRealGroup(elements, groups);
		
		HashMap<String,String> options=new HashMap<String,String>();

		
		HashMap<String, Element> elements=null;
		HashMap<String,Group> groups=null;
		try {
			elements = LoadFromFiles.elements("data/atom1995.txt","data/isotope1995.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	//	testIsotope(elements, groups, options);
//	testBug2(elements, groups, options);
	//	testPolymer(elements, groups, options);
		testSimple(elements, groups, options);
	// 	testPeptide(elements, groups, options);
	//	fullAnalysis(elements, groups, options);
	//	fullAnalysisPeptide(elements, groups, options);
		
	}
	
	public static void fullAnalysis(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "C0-200 H0-400 N0-50 O0-50 F0-10 Cl0-10 Br0-10 S0-10 P0-10");
		options.put("massRange", "0.01");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "true");

		
		System.out.println("Target MS\tBrute force\tReal\tRatio\tNb results\tTime (ms)");
		JSONObject json;
		for (int i=100; i<2000; i++) {
			options.put("monoisotopicMass",i+"");
			long time=System.currentTimeMillis();
			json=new MFFromMonoisotopicMass().execute(options,groups,elements);
			time=System.currentTimeMillis()-time;
			System.out.println(i+"\t"+json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		}
	}

	public static void fullAnalysisPeptide(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "H2OAla0-20Arg0-20Asn0-20Asp0-20Cys0-20Gln0-20Glu0-20Gly0-20His0-20Leu0-20Lys0-20Met0-20Phe0-20Pro0-20Ser0-20Thr0-20Trp0-20Tyr0-20Val0-20");
		options.put("mfRange", "H2OAla0-20Arg0-20Asn0-20Asp0-20Cys0-20Gln0-20Glu0-20Gly0-20His0-20Ile0-20Leu0-20Lys0-20Met0-20Phe0-20Pro0-20Ser0-20Thr0-20Trp0-20Tyr0-20Val0-20");
		options.put("massRange", "0.005");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultssOnly", "true");

		
		System.out.println("Target MS\tBrute force\tReal\tRatio\tNb results\tTime (ms)");
		JSONObject json;
		for (double i=1000.005; i<1001; i=i+0.01) {
			options.put("monoisotopicMass",i+"");
			long time=System.currentTimeMillis();
			json=new MFFromMonoisotopicMass().execute(options,groups,elements);
			time=System.currentTimeMillis()-time;
			System.out.println(i+"\t"+json.getString("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		}
	}

	public static void testPolymer(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "{C2H4}0-10");
		options.put("massRange", "0.2");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("monoisotopicMass","112.125");
		long time=System.currentTimeMillis();
		JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
		time=System.currentTimeMillis()-time;
		System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		printResult(json);
	} 
	
	public static void testIsotope(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "[13C]0-100[12C]0-100[15N]H0-100");
		options.put("massRange", "0.2");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("monoisotopicMass","1200");
		long time=System.currentTimeMillis();
		JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
		time=System.currentTimeMillis()-time;
		System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		printResult(json);
	} 
	
	public static void testPeptide(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "H2OAla0-20Arg0-20Asn0-20Asp0-20Cys0-20Gln0-20Glu0-20Gly0-20His0-20Ile0-20Leu0-20Lys0-20Met0-20Phe0-20Pro0-20Ser0-20Thr0-20Trp0-20Tyr0-20Val0-20");
		options.put("massRange", "0.01");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		for (int i=1300; i<1501; i=i+50) {
			options.put("monoisotopicMass",i+"");
			long time=System.currentTimeMillis();
			JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
			time=System.currentTimeMillis()-time;
			System.out.println(i+":"+json.getInt("numberResults")+" ("+time/1000+"s)");
			//System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
			// printResult(json);
		}
	}
	
	public static void testBug(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "[13C]1-100");
		options.put("massRange", "1");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("monoisotopicMass","156");
		long time=System.currentTimeMillis();
		JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
		time=System.currentTimeMillis()-time;
		//System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		printResult(json);
	}
	
	public static void testBug2(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "C0-10As1-10");
		options.put("massRange", "1");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("monoisotopicMass","24.1");
		long time=System.currentTimeMillis();
		JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
		time=System.currentTimeMillis()-time;
		//System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		printResult(json);
	}
	
	public static void testSimple(HashMap<String,Element> elements, HashMap<String,Group> groups, HashMap<String,String> options) throws JSONException {
		options.put("mfRange", "C0-50H0-50O0-10F0-10(-)");
		options.put("massRange", "2");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("monoisotopicMass","112.98560");
		options.put("typedResult","true");
		long time=System.currentTimeMillis();
		JSONObject json=new MFFromMonoisotopicMass().execute(options,groups,elements);
		time=System.currentTimeMillis()-time;
		System.out.println(json.getLong("bruteForceIteration")+"\t"+json.getInt("realIteration")+"\t"+json.getLong("bruteForceIteration")/json.getInt("realIteration")+"\t"+json.getInt("numberResults")+"\t"+time);
		printResult(json);
	}
	
	public static void printResult(JSONObject json) {
		System.out.println(json.toString());
		try {
			JSONArray results=json.getJSONArray("results");
			for (int i=0; i<results.length(); i++) {
				JSONObject result=results.getJSONObject(i);
				System.out.println(i+" - "+result.getString("mf")+" - "+result.getDouble("em"));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void initRealElement(HashMap<String,Element> elements) {
		elements.put("C", new Element(12,12,"C","Carbon",6));
		elements.put("O", new Element(15.994915,15.994915,"O","Oxygen",8));
		elements.put("N", new Element(14.003074,14.003074,"N","Nitrogen",7));
		elements.put("Cl", new Element(34.968852,34.968852,"Cl","Chlorine",17));
		elements.put("H", new Element(1.007825,1.007825,"H","Hydrogen",1));
		elements.put("B", new Element(11.009305,11.009305,"B","Bore",5));
		elements.put("P", new Element(30.973762,30.973762,"P","Phosphorus",15));
		elements.put("F", new Element(18.998403,18.998403,"F","Fluorine",9));
		elements.put("Br", new Element(78.918336,78.918336,"Br","Bromine",35));
		elements.put("I", new Element(126.904473,126.904473,"I","Iodine",53));
		elements.put("S", new Element(31.97207,31.97207,"S","Sulfur",16));
	}
	
	public static void initRealGroup(HashMap<String,Element> elements, HashMap<String,Group> groups) throws MFException {
		 groups.put("Ala",new Group("Ala","Alanine","C3H5NO", elements, groups));
		 groups.put("Arg",new Group("Arg","Arginine","C6H12N4O", elements, groups));
		 groups.put("Asn",new Group("Asn","Asparagine","C4H6N2O2", elements, groups));
		 groups.put("Asp",new Group("Asp","Aspartame","C4H5NO3", elements, groups));
		 groups.put("Cys",new Group("Cys","Castéine","C3H5NOS", elements, groups));
		 groups.put("Gln",new Group("Gln","Glutamine","C5H8N2O2", elements, groups));
		 groups.put("Glu",new Group("Glu","Glutamate","C5H7NO3", elements, groups));
		 groups.put("Gly",new Group("Gly","Glycine","C2H3NO", elements, groups));
		 groups.put("His",new Group("His","Histidine","C6H7N3O", elements, groups));
		 groups.put("Ile",new Group("Ile","Isoleucine","C6H11NO", elements, groups));
		 groups.put("Leu",new Group("Leu","Leucine","C6H11NO", elements, groups));
		 groups.put("Lys",new Group("Lys","Lysine","C6H12N2O", elements, groups));
		 groups.put("Met",new Group("Met","Methionine","C5H9NOS", elements, groups));
		 groups.put("Phe",new Group("Phe","Phenylalanine","C9H9NO", elements, groups));
		 groups.put("Pro",new Group("Pro","Proline","C5H7NO", elements, groups));
		 groups.put("Ser",new Group("Ser","Serine","C3H5NO2", elements, groups));
		 groups.put("Val",new Group("Val","Valine","C5H9NO", elements, groups));
		 groups.put("Thr",new Group("Thr","Threonine","C4H7NO2", elements, groups));
		 groups.put("Trp",new Group("Trp","Tryptophane","C11H10N2O", elements, groups));
		 groups.put("Tyr",new Group("Tyr","Tyrosine","C9H9NO2", elements, groups));
	}
	
	static void initDebugElement(HashMap<String,Element> elements) {
		elements.put("C", new Element(12,12,"C","Carbon",6));
		elements.put("O", new Element(16,16,"O","Oxygen",8));
		elements.put("N", new Element(14,14,"N","Nitrogen",7));
		elements.put("Cl", new Element(35,35,"Cl","Chlorine",17));
		elements.put("H", new Element(1,1,"H","Hydrogen",1));
		elements.put("B", new Element(4,4,"B","Bore",5));
		elements.put("P", new Element(31,31,"P","Phosphorus",15));
		elements.put("D", new Element(2,2,"D","Deuterium",1));
	}
	
}
