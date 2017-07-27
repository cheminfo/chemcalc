package org.chemcalc.test;

import org.chemcalc.core.Element;
import org.chemcalc.core.Group;
import org.chemcalc.core.LoadFromFiles;
import org.chemcalc.core.MFException;
import org.chemcalc.services.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class Main201502 {

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
	public void json() throws MFException, JSONException {
		init();
		
		options.put("mf", "C50H50C50H50");
		options.put("isotopomers", "");
		options.put("resolution", "0.001");
		JSONObject json=JSONForMF.execute(options,groups,elements);

		Assert.assertEquals(json.getDouble("em"),1300.7825,0.00001);
		Assert.assertEquals(json.getDouble("mw"),1301.894,0.1);
		Assert.assertEquals(json.getString("mf"),"C100H100");
		
		JSONObject firstPart=json.getJSONArray("parts").getJSONObject(0);
		Assert.assertEquals(firstPart.getInt("unsaturation"),51);
		Assert.assertEquals(firstPart.getDouble("msem"),1300.7825,0.00001);
		Assert.assertEquals(firstPart.getJSONArray("ea").getJSONObject(0).getDouble("percentage"),92.257895,0.001);
		
		// we change dataVersion
		options.put("referenceVersion","2012");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getDouble("em")+"","1300.782503214");
		Assert.assertEquals(json.getDouble("mw")+"","1301.867665");
		Assert.assertEquals(json.getString("mf"),"C100H100");
		
		// we come back to the original one
		options.put("referenceVersion","1995");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getDouble("em")+"","1300.7825");
		Assert.assertEquals(json.getDouble("mw")+"","1301.894");
		Assert.assertEquals(json.getString("mf"),"C100H100");
		
		options.put("referenceVersion","2012");
		json=JSONForMF.execute(options,groups,elements);
	}
	
	@Test
	public void largeResult() throws MFException, JSONException {
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
		
		options.put("mf", "C1000000H1000000");
		options.put("isotopomers", "");
		options.put("resolution", "0.0001");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		
		Assert.assertEquals(json.getDouble("em"),13007825,0.00001);
		Assert.assertEquals(json.getDouble("mw"),13018940,0.00001);
		Assert.assertEquals(json.getInt("nominalMass"),13000000);
		Assert.assertEquals(json.getString("mf"),"C1000000H1000000");
	}
	
	@Test
	public void isotopicDistribution() throws MFException, JSONException {
		init();
		
		options.put("mf", "C1000000");
		options.put("isotopomers", "xy");
		options.put("resolution", "0.001");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		
		Assert.assertEquals(json.getDouble("em"),1.2E7,0.00001);
		Assert.assertEquals(json.getDouble("mw"),1.2011E7,0.001E7);
		
		
		
		Assert.assertEquals(json.getString("mf"),"C1000000");
		Assert.assertEquals(json.getString("xy").indexOf("12011036"),18448);
	}
	
	@Test
	public void isotopicDistributionRu() throws MFException, JSONException {
		init();
		
		options.put("mf", "Ru20");
		options.put("isotopomers", "xy");
		options.put("resolution", "0.001");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		
		Assert.assertEquals(json.getDouble("em"),2038.08696,0.0001);
		Assert.assertEquals(json.getDouble("mw"),2021.298,0.001);
		Assert.assertEquals(json.getString("mf"),"Ru20");
		
		// System.out.println(json.getString("xy"));
		Assert.assertEquals(json.getString("xy").indexOf("2025.101"),10683);
	}
	
	@Test
	public void isotopicDistributionRuV2() throws MFException, JSONException {
		init();
		
		options.put("mf", "Ru20");
		options.put("isotopomers", "xy");
		options.put("resolution", "0.001");
		options.put("joiningAlgorithm","center");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		
		Assert.assertEquals(json.getDouble("em"),2038.08696,0.0001);
		Assert.assertEquals(json.getDouble("mw"),2021.2981,0.01);
		Assert.assertEquals(json.getString("mf"),"Ru20");
		
		// System.out.println(json.getString("xy"));
		Assert.assertEquals(json.getString("xy").indexOf("2022.10172, 100"),2133);
	}

	@Test
	public void isotopicDistributionC100H100() throws MFException, JSONException {
		init();

		options.put("mf", "C100H100");
		options.put("isotopomers", "xy");
		options.put("fwhm", "0.1");
		options.put("joiningAlgorithm","center");
		JSONObject json=JSONForMF.execute(options,groups,elements);

		Assert.assertEquals(json.getDouble("em"),1300.7825032071,0.0001);
		Assert.assertEquals(json.getDouble("mw"),1301.867665,0.01);
		Assert.assertEquals(json.getString("mf"),"C100H100");

		// System.out.println(json.getString("xy"));
		Assert.assertEquals(json.getString("xy").indexOf("1301.78589, 100"),20);
	}

	
	@Test
	public void specialIsotopicDistribution() throws MFException, JSONException {
		init();
		
		options.put("mf", "C{49,51}10");
		options.put("isotopomers", "xy");
		options.put("resolution", "0.001");
		JSONObject json=JSONForMF.execute(options,groups,elements);

		Assert.assertEquals(json.getDouble("em"),130.03355,0.00001);
		Assert.assertEquals(json.getInt("nominalMass"),130);
		
		options.put("mf", "Cl{49,51}10");
		options.put("isotopomers", "xy");
		options.put("resolution", "0.001");
		json=JSONForMF.execute(options,groups,elements);
		
		Assert.assertEquals(json.getDouble("em"),369.65903,0.00001);
		Assert.assertEquals(json.getInt("nominalMass"),370);
	}
	
	@Test
	public void MFRangeIntersection() throws MFException, JSONException {
		init();
		
		options.put("mf1", "C3-10H1-20");
		options.put("mf2", "C10H12");
		JSONObject json=MFRangeIntersection.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C3-10H1-12");
		
		options.put("mf1", "C3-10H1-20Cl"); // we need one Cl 
		options.put("mf2", "C10H12");
		json=MFRangeIntersection.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"");

		options.put("mf1", "C3-5H1-10");
		options.put("mf2", "C10H12Cl");
		json=MFRangeIntersection.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C3-5H1-10");

		options.put("mf1", "C3-5H1-10Cl");
		options.put("mf2", "C10H12Cl");
		json=MFRangeIntersection.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C3-5H1-10Cl1");
		
		options.put("mf1", "C10H12Cl");
		options.put("mf2", "C10H12Cl");
		json=MFRangeIntersection.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C0-10H0-12Cl0-1");
		
	}
	
	@Test
	public void testSimple() throws JSONException, MFException {
		init();
		
		options.put("mf1", "C10H12Cl");
		options.put("mf2", "C3H10Cl");
		JSONObject json=MFDiff.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C7H2");
		
		options.put("mf1", "C10H12Cl");
		options.put("mf2", "C15H10Cl2");
		json=MFDiff.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C-5H2Cl-1");
		
		options.put("mf1", "C10H12ClBr");
		options.put("mf2", "C3H10ClNO2");
		json=MFDiff.execute(options,groups,elements);
		Assert.assertEquals(json.getString("mf"),"C7H2BrN-1O-2");
		
	}
	
	@Test
	public void mfFromMonosotopicMass() throws JSONException, MFException {
		init();
		
		options.put("mfRange", "C0-10H0-20");
		options.put("massRange", "2");
		options.put("useUnsaturation", "false");
		options.put("numberOfResultsOnly", "false");
		options.put("integerUnsaturation", "false");
		options.put("monoisotopicMass","125.039125");
		options.put("typedResult","false");
		JSONObject json=MFFromMonoisotopicMass.execute(options,groups,elements);
		Assert.assertEquals(json.getInt("bruteForceIteration"),231);
		
		Assert.assertEquals(json.getJSONArray("results").length(),7);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getString("mf"),"C10H5");
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("unsat"),8.5,0.00001);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("error"),0,0.00001);
		
		options.put("useUnsaturation", "true");
		options.put("minUnsaturation", "3");
		json=MFFromMonoisotopicMass.execute(options,groups,elements);
		Assert.assertEquals(json.getInt("bruteForceIteration"),231);
		Assert.assertEquals(json.getJSONArray("results").length(),3);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getString("mf"),"C10H5");
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("unsat"),8.5,0.00001);
		
		options.put("integerUnsaturation", "true");
		json=MFFromMonoisotopicMass.execute(options,groups,elements);
		Assert.assertEquals(json.getInt("bruteForceIteration"),231);
		Assert.assertEquals(json.getJSONArray("results").length(),2);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getString("mf"),"C10H4");
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("unsat"),9.0,0.00001);
		
		options.put("mfRange", "C0-50H0-50(-2)");
		options.put("integerUnsaturation", "false");
		json=MFFromMonoisotopicMass.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getString("mf"),"C20H10(-2)");
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("error"),0.0005485799,0.00001);
		
		options.put("mfRange", "C0-50H0-50(++)");
		options.put("integerUnsaturation", "false");
		json=MFFromMonoisotopicMass.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getString("mf"),"C20H10(+2)");
		Assert.assertEquals(json.getJSONArray("results").getJSONObject(0).getDouble("error"),-0.0005485799,0.00001);
	}

	
	@Test
	public void Charge() throws MFException, JSONException {
		init();
		
		options.put("mf", "Ru20+");
		JSONObject json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),1.0,0.00001);

		options.put("mf", "NH4++");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),2.0,0.00001);
		
		options.put("mf", "Cl2-");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),-1.0,0.00001);
		
		options.put("mf", "HLys(H+)Lys(H+)OH");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),2.0,0.00001);
		
		options.put("mf", "HLys(H+)Arg(H+)OH-");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),1.0,0.00001);
		
		options.put("mf", "C(+2)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),2.0,0.00001);
		
		options.put("mf", "C(-2)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),-2.0,0.00001);
		
		options.put("mf", "C(2+)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),2.0,0.00001);
		
		options.put("mf", "C2(2-)(+3)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),1.0,0.00001);
		
		options.put("mf", "C(++++)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),4.0,0.00001);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getString("mf"),"C(+4)");
		
		options.put("mf", "C(---)");
		json=JSONForMF.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getDouble("charge"),-3.0,0.00001);
		Assert.assertEquals(json.getJSONArray("parts").getJSONObject(0).getString("mf"),"C(-3)");
	}
	
	@Test
	public void getInfo() throws JSONException, MFException {
		init();
		
		JSONObject json=GetInfo.execute(options,groups,elements);
		Assert.assertEquals(json.getJSONArray("elements").length(),119);
		Assert.assertEquals(json.getJSONArray("groups").length(),110);
	}
	
}
