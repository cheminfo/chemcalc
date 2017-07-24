package org.chemcalc.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines an isotope of a chemical element.
 *
 * @author Michal Krompiec <mkrom@zeus.polsl.gliwice.pl>
 * @version 05 Jul 2002
 *
 * @see Element
 * @see Atom
 */
public class Isotope implements Comparable {
	/**
	 * Defines an isotope of a chemical element.
	 * @param elementSymbol the symbol of the chemical element
	 * @param mass atomic mass of the isotope (in [u]) 
	 * @param number mass number of the isotope
	 * @param percentage the percentage of this isotope in natural element sample
	 */
    
	public Isotope(int number, double mass, double percentage, String elementSymbol) {
		this.number=number;
		this.mass=mass;
		this.percentage=percentage;
		this.elementSymbol=elementSymbol;
		// System.out.println("loading isotope: "+this.toString());
	}
	 

	 /** 
	  * Mass number. 
	  */
	 public int number;
 
 /** 
  * Atomic mass of this isotope (in [u]). 
  */
 public double mass;
 
 /** 
  * Percentage of this isotope in natural element. Percentage should be 
  * between 0 and 100.
  */
 public double percentage;
 
 /** 
  * Atomic symbol. 
  */
 public String elementSymbol;
 
 /**
  * Compares isotope numbers.
  */
 public int compareTo(Object o) {
 	if (o instanceof Isotope) {
 		Isotope is=(Isotope)o;
 		return this.number=is.number;
 	} else throw new UnsupportedOperationException("Cannot compare Isotope to "+o);
 }
 
	public String toString() {
		return "Number: "+number+" - mass: "+mass+" - percentage: "+percentage+" - symbol: "+elementSymbol;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject jsonIsotope=new JSONObject();
		jsonIsotope.put("mass", this.mass);
		jsonIsotope.put("percentage", this.percentage);
		return jsonIsotope;
	}
	
}