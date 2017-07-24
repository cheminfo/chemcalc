package org.chemcalc.services.test;

import org.chemcalc.core.*;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

public class TestFormulaUTF8 {

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

		Formula formula=new Formula("[13C]5C10H12O4Br2", elements, groups);
		
		System.out.println(formula.toString());
		System.out.println(formula.toHtml(false));
		System.out.println(formula.toHtml(true));
	}
	


	


}
