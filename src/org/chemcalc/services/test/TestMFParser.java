package org.chemcalc.services.test;


import org.chemcalc.core.*;

import java.io.IOException;
import java.util.HashMap;


public class TestMFParser {


	static String[] mfs={"C(-)","C(+)","(C(+))2","(C(-))2","(C(+2))2","(C(-2))2","(C(2+))2","(C(2-))2","C((+2)2)2","C(((+))2)2"};

	
	public static void main(String[] args) throws MFException {
		
		
		HashMap<String,Element> elements=new HashMap<String,Element>();
		HashMap<String,Group> groups=new HashMap<String,Group>();

		try {
			elements = LoadFromFiles.elements("data/atom201502.txt","data/isotope201502.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		for (int i=0; i<mfs.length; i++) {
			Formula formula= new Formula(mfs[i], elements, groups);
			System.out.println(mfs[i]+" - "+formula.toString());
		}
	}
}
