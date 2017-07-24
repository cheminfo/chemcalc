package org.chemcalc.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
/**
 * Defines a chemical element: its name, mass, exact mass and isotopes.
 *
 * @author Michal Krompiec
 * @version 09 Jul 2002
 *
 * @see Isotope
 * @see Atom 
 * @see Group
 */
public class Element implements Comparable {

 	private double monoisotopicMass;
 	private int nominalMass;
 	private String symbol;
 	private double mass;
 	private String name;
 	private int atomicNumber;
 	
 	final static boolean DEBUG=false;
 	/**  
 	 * Contains isotopes as <code>Isotope</code> objects. 
 	 * The key is the mass number.
 	 *
 	 * @see Isotope
 	 */
 	private HashMap<Integer,Isotope> isotopes;
 		
 	
    public Element() {
    	this(0,0,"","",0);
    }
    
    public int getNominalMass() {
		return nominalMass;
	}
    
    public int getAtomicNumber() {
    	return atomicNumber;
    }

	public void setNominalMass(int nominalMass) {
		this.nominalMass = nominalMass;
	}

	public void setMonoisotopicMass(double monoisotopicMass) {
		this.monoisotopicMass = monoisotopicMass;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setAtomicNumber(int atomicNumber) {
		this.atomicNumber = atomicNumber;
	}

	public Element(double mass, String symbol, String name, int atomicNumber) {
    	this(mass, 0, symbol, name, atomicNumber);
    }
    
    public Element(Element element, String symbol, String name) {
    	// the name will contain the atom description that can be put back in the MF later ...
    	this.name=name;
    	this.symbol=symbol;
    	this.atomicNumber=element.atomicNumber;
    	this.isotopes=new HashMap<Integer,Isotope>();
    	String isotopesString=name.replaceAll("[^0-9,\\.]", "");
    	String[] parts=isotopesString.split(",");
    	int i=0;
    	if (DEBUG) System.out.println("Element: copy existing element: "+element.toString()+" - "+isotopesString+" - "+parts.length);
    	for (Isotope isotope : element.isotopes.values()) {
    		if (DEBUG) System.out.println("Old isotope: "+isotope.toString());
    		double percent=0;
    		if (parts.length>i) percent=Double.parseDouble(parts[i]);
    		Isotope newIsotope=new Isotope(isotope.number, isotope.mass, percent, isotope.elementSymbol);
    		this.isotopes.put(isotope.number, newIsotope);
    		if (DEBUG) System.out.println("Adding isotope: "+newIsotope.toString());
    		i++;
    	}

    	calculateMass();
    	calculateMonoisotopicAndNominalMass();
    	if (DEBUG) System.out.println("New element created: "+this.toString());
    	if (DEBUG) System.out.println("Old element is now: "+element.toString());
    }
    
    
/**
 * Defines an element by the atomic mass, exact mass and symbol.
 *
 * @param mass atomic mass [u]
 * @param exactMass exact mass [u]
 * @param symbol atomic symbol
 * @param name name of the element
 */
    public Element(double mass, double exactMass, String symbol, String name, int atomicNumber) {
        isotopes=new HashMap<Integer,Isotope>();
        this.mass=mass;
        this.monoisotopicMass=exactMass;
        this.symbol=symbol;
        this.name=name;
        this.atomicNumber=atomicNumber;
    }
    

    
 /** 
  * Returns the atomic mass. 
  *
  * @return atomic mass
  */
 	public double getMass() {
 		return mass;
 	}


 /** 
  * Returns the monoisotopic mass. The monoisotopic mass is defined as the mass 
  * of the more abundant isotope. 
  *
  * @return monoisotopic mass, mass of the more abudant isotope
  *
  * @throws RuntimeException if the monoisotopic mass is not known (that is, 
  * if it is equal to 0)
  */
    public double getMonoisotopicMass() {
        if (monoisotopicMass==0) throw new RuntimeException("Exact mass of "+getSymbol()
            +" is unknown. Run calculateExactMass() first.");
 		return monoisotopicMass;
 	}

/** 
  * Calculates the monoisotopic mass. The monoisotopic mass is defined as the mass 
  * of the more abundant isotope. This function should be called when the 
  * isotopes are loaded and the monoisotopic mass is still not known.
  */    
    public void calculateMonoisotopicAndNominalMass() {
        if (isotopes.isEmpty()) {
            monoisotopicMass=mass;
            nominalMass=(int)Math.round(mass);
            return;
        }
        double em=Double.MAX_VALUE;
        double percentage=Double.MIN_VALUE;
        boolean first=true;
        for (Isotope isotope : isotopes.values()) {
        	// if no isotope are present we take the first isotope for the monoisotopic mass
            if (isotope.percentage>percentage || first) {
            	first=false;
            	em=isotope.mass;
            	percentage=isotope.percentage;
            }
        }
        
        monoisotopicMass=em;
        nominalMass=(int)Math.round(em);
    }
 	
    /** 
     * Calculates the  mass.
     */
    
   public void calculateMass() {
       if (isotopes.isEmpty()) {
           return;
       }
       double mass=0;
       for (Isotope isotope : isotopes.values()) {
    	   mass+=isotope.mass*isotope.percentage/100;
       }
       if (mass==0) return;
       this.mass=mass;
   }
    

 /** 
  * Returns the atomic symbol.
  *
  * @return atomic symbol
  */ 
	public String getSymbol() {
		return symbol;
	}
 
/** 
 * Returns the number of isotopes of this element.
 */ 
	public int getNumIsotopes() {
 		return isotopes.size();
 	}

/**
 * Returns the name of this element.
 */    
    public String getName() {
        return name;
    }

 	
/**
 * Adds the isotope  <code>is</code> to the list of the isotopes of this element.
 *
 * @see #getIsotopes 
 * @see #getIsotope
 * @see Isotope
 */	
	public void addIsotope(Isotope isotope) {
		isotopes.put(new Integer(isotope.number),isotope);
	}
	
/** 
 * Returns the isotope having the mass number equal to <code>number</code>.
 *
 * @param number mass number of the isotope
 * @return the isotope with the mass number being equal to <code>number</code> 
 * or null if not found
 *
 * @see #addIsotope
 * @see #getIsotopes
 * @see Isotope
 */
	public Isotope getIsotope(int number) {
		return (Isotope)isotopes.get(new Integer(number));
	}
	
	public String toString() {
		
		String toReturn="Element: "+this.atomicNumber+" - symbol: "+this.symbol+" - exact mass: "+this.monoisotopicMass+" - mass: "+this.mass+" - name: "+this.name+" - nb isotope: "+this.isotopes.size()+"\r\n";
		for (Isotope isotope : isotopes.values()) {
			toReturn+=isotope.toString()+"\r\n";
		}
		return toReturn;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json=new JSONObject();
		json.put("symbol", this.symbol);
		json.put("monoisotopicMass", this.monoisotopicMass);
		json.put("mass", this.mass);
		json.put("name", this.name);
		json.put("atomicNumber", this.atomicNumber);
		JSONArray jsonIsotopes=new JSONArray();
		json.put("isotopes", jsonIsotopes);
		
		for (Isotope isotope : isotopes.values()) {
			JSONObject jsonIsotope=isotope.toJSON();
			jsonIsotopes.put(jsonIsotope);
		}
		return json;
	}
	
/** 

 */
	public HashMap<Integer,Isotope> getIsotopes() {
		return isotopes;
	}
	


	@Override
	public int compareTo(Object otherElement) {
		System.out.println(this.getSymbol()+"-"+((Element)otherElement).getSymbol());
		
		return this.getSymbol().compareTo(((Element)otherElement).getSymbol());
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
	        return true;
	    if (this == null)
	        return false;
	    if (this.getClass() != obj.getClass())
	        return false;
	    final Element other = (Element) obj;
	    if (this.getSymbol().equals(other.getSymbol())) return true;
	    return false;
	}
	
}
