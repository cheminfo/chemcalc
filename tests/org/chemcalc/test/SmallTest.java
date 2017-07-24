package org.chemcalc.test;

import org.chemcalc.core.MFException;
import org.json.JSONException;

import java.util.Vector;

public class SmallTest {

	public static void main(String[] args) throws JSONException, MFException {
		

		SmallTest st=new SmallTest();
		Vector ab=new Vector();
		System.out.println("Before: "+ab.toString());
		st.test(ab);
		System.out.println("After: "+ab.toString());
	}

	public void test(Vector ab) {
		// ab=new Vector();
		ab.add("Test");
		System.out.println(ab.toString());
	}
	
	
}
