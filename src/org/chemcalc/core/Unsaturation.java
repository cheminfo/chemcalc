package org.chemcalc.core;

import java.util.HashMap;
import java.util.Vector;

 public class Unsaturation {

	 private static HashMap<String,Integer> unsaturation=new HashMap<String,Integer>();
 
	 
	 static {
		 unsaturation.put("O", 0);
		 unsaturation.put("N", 1);
		 unsaturation.put("H", -1);
		 unsaturation.put("C", 2);
		 unsaturation.put("F", -1);
		 unsaturation.put("Cl", -1);
		 unsaturation.put("Br", -1);
		 unsaturation.put("I", -1);
		 unsaturation.put("S", 0);
		 unsaturation.put("P", 1);
		 unsaturation.put("Li", -1);
		 unsaturation.put("Na", -1);
		 unsaturation.put("K", -1);
		 unsaturation.put("Ca", 0);
		 unsaturation.put("Mg", 0);
	 }
	 
	 private static Integer getUnsaturation(String atom) {
		 if (unsaturation.containsKey(atom)) {
			 return unsaturation.get(atom);
		 }
		 return null;
	 }

	 
	 // TODO - 2 times the same code !!!!!! or nearly
	 
	 public static Double getUnsaturation(FormulaPart atoms, Double defaultUnsaturationContribution) {
		 double unsaturation=2;
		 for (Atom atom : atoms) {
			 Integer currentUnsaturation=getUnsaturation(atom.symbol);
			 if (currentUnsaturation==null) {
				 if (defaultUnsaturationContribution==null) {
					 return defaultUnsaturationContribution;
				 } else {
					 unsaturation+=defaultUnsaturationContribution*atom.getCount();
				 }
			 } else {
				 unsaturation+=currentUnsaturation*atom.getCount();
			 }
		 }
		 return new Double(((double)unsaturation+atoms.charge)/2);
	 }
	 
	public static Double getUnsaturation(Vector<ExtendedAtom> atoms, Double defaultUnsaturationContribution) {
		 double unsaturation=2;
		 for (ExtendedAtom atom : atoms) {
			 Integer currentUnsaturation=getUnsaturation(atom.atom.symbol);
			 if (currentUnsaturation==null) {
				 if (defaultUnsaturationContribution==null) {
					 return defaultUnsaturationContribution;
				 } else {
					 unsaturation+=defaultUnsaturationContribution*atom.currentCount;
				 }
			 } else {
				 unsaturation+=currentUnsaturation*atom.currentCount;
			 }
		 }
		 return new Double((double)unsaturation/2);
	 }
}
