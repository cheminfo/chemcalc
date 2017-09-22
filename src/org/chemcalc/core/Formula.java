package org.chemcalc.core;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;


/** 
 * A molecular formula. Can be multipart. An example of multipart formula is PhNH2.HCl 
 * (aniline hydrochloride) - PhNH2 and HCl are two <i>parts</i> of this formula, each is 
 * represented by a separate <code>FormulaPart</code> object.
 * <p>Can be a <i>range</i> formula (for example 
 * C<sub>3-4</sub>H<sub>6-8</sub>) or a defined one (for example CH4).
 * <p><i>Range</i> formulae are used for database queries. Their mass is undefined.
 *
 * @author Michal Krompiec
 * @version 08 Jul 2002
 */
public class Formula {
/** Contains RangeFormula objects. For a formula "NH3.BF3", parts contains "NH3" and "BF3" */
	private ArrayList<FormulaPart> parts;
	private boolean containsIsotopes;

    private HashMap<String,Element> elements;
    private HashMap<String,Group> groups;
    
	private boolean isRange;
	private boolean combinatorial;
	
/**
 * Creates a molecular formula. 
 * <p> Examples of syntax: see <a href="http://www.chemcalc.org">ChemCalc</a>
 * For multipart formulae, the number before each part (for example, "5" in 
 * CuSO4.5H2O) is stored in <code>FormulaPart.number</code>.
 * @param molecularFormula the molecular formula
 * @param elements chemical elements
 * @param groups groups of atoms
 *
 * @see FormulaPart
 * @see LoadFromDB#elements
 * @see LoadFromDB#groups
 */	
    public Formula(String molecularFormula, HashMap<String,Element> elements, HashMap<String,Group> groups, boolean expandGroup) throws MFException{
        this.elements=elements;
        this.groups=groups;
        parts=new ArrayList(3);
        parse(molecularFormula, expandGroup);
        if (parts.isEmpty()) 
        	throw new MFException("Cannot parse: empty formula: "
        		+"->"+molecularFormula+"<-");
    }

    public Formula(String molecularFormula, HashMap<String,Element> elements, HashMap<String,Group> groups) throws MFException{
    	this(molecularFormula, elements, groups, true);
    }
    
/**
 * Returns an iterator over parts of this formula.
 */
    public Iterator<FormulaPart> getPartsIterator() {
        return parts.iterator();
    }
    
    public ArrayList<FormulaPart> getParts() {
        return parts;
    }
    
/**
 *  Returns part no. <code>which</code> (parts are numerated starting with 1).
 *  @param which the number of the part to return. Note: first part is 1, not 0.
 *  @throws IndexOutOfBoundsException if part no. <code>which</code> does not exist
 */    
    public FormulaPart getPart(int which) {
        return (FormulaPart)parts.get(which - 1);
    }


/**
 * Returns true if any of the atoms in any part of this formula is 
 * a specified isotope.
 * @return true if for any of the atoms comprising the molecule 
 * the isotope number is specified
 */ 
    public boolean containsIsotopes() {
       return containsIsotopes;
    }


/**
 * Returns true if the formula is a <i>range</i> formula. Example of a range formula: 
 * Bu(OCH<sub>2</sub>CH<sub>2</sub>)<sub>3-4</sub>OH. 
 * Such formulas are used in database queries.
 * Mass of such a formula is not defined.
 */
	public boolean isRange() {
	   return isRange;
    }
/**
 * Returns true if the formula is <i>combinatorial</i>. A formula is combinatorial 
 * if it contains a {...} phrase. Example: {Ph,Me}H (means: benzene or methane).
 * Such formulae work well in ChemCalc only and should never be used for queries.
 */
    public boolean isCombinatorial() {
        return combinatorial;
    }
    
	private void parse(String formula, boolean expandGroup) throws MFException{
		
		// get rid of all spaces
		formula=formula.replaceAll(" ","");
		
        // modification for a special case
        // if we have the description of an isotope
        // like 13C, 1H, ...
		// 3.1.2017 we remove this special case ! It has bad consequences when you have complexes like 2 Co that becomes 2Co and
		// it tries to find the corresponding isotope that does not exists
        //formula=formula.replaceAll("^([0-9]+[A-Z][a-z]?)$","[$1]");
        
        if (formula.matches(".*[a-zA-Z][0-9]+-[0-9].*")) {
        	// Is also checked in FormulaPart !!!!!
        	isRange=true;
        }
        
        // in the molecular formula we allow some special atoms C{80,20} which would mean that the atom has 80% 12C and 20% 13C
		RegExp pattern=RegExp.compile("([A-Z][a-z]?\\{[0-9.,]*\\})");
		for(MatchResult result = pattern.exec(formula); result != null; result = pattern.exec(formula)) {
			String found = result.getGroup(1);
			String newAtomCode=this.getNewAtomCode(found);
			formula=pattern.replace(formula, newAtomCode);
		}
		
        // in the molecular formula we allow some special groups to be defined. They are under {} and do not contain comma
		// a group will automatically be defined for them
		pattern=RegExp.compile("(\\{[A-Za-z0-9]*\\})");
		for(MatchResult result = pattern.exec(formula); result != null; result = pattern.exec(formula)) {
			String found = result.getGroup(1);
			String newGroupCode=this.getNewGroupCode(found);
			formula=pattern.replace(formula, newGroupCode);
		}

		
 		if (formula.indexOf("}")!=-1) combinatorial=true;
		String[] formulaParts=formula.split("\\.");
		double mult=1.0;
		boolean multFound=false;
		for (String formulaPart : formulaParts) {
			if (isInteger(formulaPart)) {
				if (multFound) throw new MFException("Syntax error: 'number.number.'");
				mult=Double.parseDouble(formulaPart);
				multFound=true;
				continue;
			} 
            int slashIdx=formulaPart.indexOf("/");
			if (slashIdx!=-1) {
				if (multFound) throw new MFException("Syntax error: 'number.number/number'");
				int idx=slashIdx+1;
                /*if (isInteger(token.substring(idx))) {
                    mult=parseFraction(token);
                    continue;
                }*/
                int l=formulaPart.length();
				while (Character.isDigit(formulaPart.charAt(idx++))) {
				    if (idx>=l) break;
				}
				if (idx<l) idx--;
				mult=parseFraction(formulaPart.substring(0,idx));
				multFound=true;
                if (idx>=l) continue; else formulaPart=formulaPart.substring(idx);
			    //both 1/2H2O and 1/2.H2O are allowed
			}
			int i=0;
			for (; i<formulaPart.length(); i++)  {
				if (!Character.isDigit(formulaPart.charAt(i))) break;
		    }
			if (i>0) { //found a number before the string
				double m=Double.parseDouble(formulaPart.substring(0,i));
				formulaPart=formulaPart.substring(i);
				if (!multFound) {
					mult=m;
				} else {
					mult+=m/Math.pow(10,i);
				}
			}

            FormulaPart fp=new FormulaPart(formulaPart,elements,groups,mult,expandGroup);
            if (fp.containsIsotopes()) containsIsotopes=true;
            if (fp.getNumber()==0) undefined=true;
            if (fp.isRange()) isRange=true;
			parts.add(fp);
			mult=1;
			multFound=false;
		}
	}
	private boolean undefined=false;
/** 
 * Returns true if the number of one or more parts is undefined, as in RuCl3.nH2O.
 */	
	public boolean isUndefined() {
	   return undefined;
    } 
    
	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		}catch (NumberFormatException nfe) {return false;}
	}
	
	private double parseFraction(String s) throws MFException{
		String[] p=s.split("/");
		if (p.length!=2) throw new MFException("Syntax error: bad fraction syntax: "+s);
		try {
			int denominator=Integer.parseInt(p[1]);
			if (denominator==0) throw new MFException("Syntax error: divide by zero: "+s);
			int numerator=Integer.parseInt(p[0]);
			return ((double)numerator)/((double)denominator);
		} catch (NumberFormatException nfe) {
			throw new MFException("Syntax error: bad fraction syntax: "+s);
		}
	}
/**
 * Returns a string representation of this formula. 
 * The syntax is the same as of the input.
 *
 * @see FormulaPart#toString
 */

    public String toString() {
        Iterator it=parts.iterator();
        StringBuffer outstr=new StringBuffer("");
        boolean first=true;
        while (it.hasNext()) {
            if (first) {
                first=false;
            } else {
                outstr.append(".");
            }
            FormulaPart fp=(FormulaPart)it.next();
            if (fp.getNumber()==1) {
                outstr.append(fp.toString());
            } else if (fp.getNumber()==0) {
                outstr.append("n"+fp.toString());
            } else {
                String number = (fp.getNumber()+"").replaceAll("\\.0+$","");
                outstr.append(number+fp.toString());
            }
        }
        return outstr.toString();
    }

    
    public String toHtml() {
    	return toHtml(false);
    }
    
/**    
 * Returns this formula as formatted HTML text. 
 *
 * @see FormulaPart#toHtml
 */
    public String toHtml(boolean utf8) {
    	StringBuffer output=new StringBuffer("");
    	boolean begin=true;
    	for (FormulaPart fp : parts){
    		if (begin) {
    			begin=false;
    		} else {
    			output.append(".");
    		}
            if (fp.getNumber()==0) {
                output.append("n");
            } else if (fp.getNumber()!=1){
            	output.append(fp.getNumber());
            }
            output.append(fp.toHtml(utf8));

            
    	}
    	return output.toString();
    }
    
    
    

/**
 * Calculates the mass of the compound defined by this <i>multipart</i> formula.
 *
 * @throws MFException if the formula is a range formula
 * @return the mass in [u]
 *
 * @see Atom#getMass 
 * @see FormulaPart#getMass
 */    
    public double getMass() throws MFException{
        if (isRange) throw new MFException("Cannot calculate mass: this is a range formula");
        if (undefined) throw new MFException("The mass is not defined");
        double mass=0;

        for (FormulaPart formulaPart : parts) {
            mass+=formulaPart.getMass()*formulaPart.getNumber();
        }
        return round6(mass);
    }
    
    private double round6(double what) {
        double r=Math.rint(what*1e6);
        return r/1e6;
    }    

/**
 * Calculates the monoisotopic mass of the compound defined by this <i>multipart</i> formula. 
 *
 * @throws MFException if the formula is a range formula
 * @return the exact mass
 *
 * @see Atom#getMonoisotopicMass 
 * @see FormulaPart#getMonoisotopicMass
 */

    public double getMonoisotopicMass() throws MFException {
        if (isRange) throw new MFException("Cannot calculate mass: this is a range formula");
        if (undefined) throw new MFException("The mass is not clearly defined");
        double exactMass=0;
        for (FormulaPart fp : parts) {
            exactMass+=fp.getMonoisotopicMass()*fp.getNumber();
        }                             
        return round10(exactMass);
    }
    
    public int getNominalMass() throws MFException {
        if (isRange) throw new MFException("Cannot calculate mass: this is a range formula");
        if (undefined) throw new MFException("The mass is not clearly defined");
        int nominalMass=0;
        for (FormulaPart fp : parts) {
        	nominalMass+=fp.getNominalMass()*fp.getNumber();
        }                             
        return nominalMass;
    }

    public double getUnsaturation(Double defaultUnsaturationContribution) throws MFException {
        if (isRange) throw new MFException("Cannot calculate unsaturation: this is a range formula");
        if (undefined) throw new MFException("The unsaturation is not clearly defined");
        
         double unsaturation=0;
         for (FormulaPart fp : parts) {
        	 unsaturation+=Unsaturation.getUnsaturation(fp, defaultUnsaturationContribution)*fp.number;
         }  
        return round10(unsaturation);
    }
    
    
    
    
    public Collection<Atom> calculateElementalAnalysis() throws MFException {
        if (isRange) throw new MFException("Cannot calculate unsaturation: this is a range formula");
        if (undefined) throw new MFException("The unsaturation is not clearly defined");
        
        // we need to calculate the total number of atoms for all the parts
        HashMap<String,Atom> atoms=new HashMap<String,Atom>();
        for (FormulaPart fp : parts) {
        	for (Atom atom : fp) {
        		if (! atoms.containsKey(atom.symbol)) {
        			Atom newAtom=new Atom(atom);
        			newAtom.minCount*=fp.number;
        			newAtom.maxCount*=fp.number;
        			atoms.put(atom.symbol, newAtom);
        		} else {
        			Atom newAtom=atoms.get(atom.symbol);
        			newAtom.minCount+=atom.minCount*fp.number;
        			newAtom.maxCount+=atom.maxCount*fp.number;
        		}
            }
        }
        double mass=this.getMass();
        for (Atom atom : atoms.values()) {
            atom.percentage=round6(atom.getMass()*atom.getCount()*100/mass);
        }
        
        return atoms.values();
    }
        
    
    public double getCharge() throws MFException {

        if (undefined) throw new MFException("The charge is not clearly defined");
        
         double charge=0;
         for (FormulaPart fp : parts) {
        	 charge+=fp.charge*fp.number;
         }  
        return round10(charge);
    }

    
    
    private double round10(double what) {
        double r=Math.rint(what*1e10);
        return r/1e10;
    }    

/**
 * Returns the number of parts (each part is represented by a <code>FormulaPart</code>
 * in this formula. For single-part formulae, returns 1.
 * @return number of parts in this formula
 *
 * @see #getPart
 * @see #getParts
 * @see FormulaPart
 */
    public int getNumberOfParts() {
       return parts.size();
    }
   
/**
 * Removes all the atoms that have zero afix.
 * Calls <code>removeZeroes()</code> for each <code>FormulaPart.</code>
 * If a formula contains atoms with zero affices,
 * it is treated as a <i>range formula</i> and
 * <code>isRange()</code> would return <code>true</code>.
 *
 * @see FormulaPart#removeZeroes()
 * @see #isRange()
 */
    public void removeZeroes() {
        if (!isRange()) return;
        for (Iterator it=getPartsIterator();it.hasNext();) {
            FormulaPart part=(FormulaPart)it.next();
            part.removeZeroes();
        }
    }
    
    // we will create a new atom code based on a string like C{20,80}
    private synchronized String getNewAtomCode(String description) {
    	// TODO because elements are static this will give a problem after a while ... Memory leak !
    	String newAtomCode=digest(description);
    	
		String atomCode=description.replaceAll("\\{.*", "");
		if (! elements.containsValue(newAtomCode)) {
			elements.put(newAtomCode, new Element(elements.get(atomCode), newAtomCode, description));
		}
		return newAtomCode;
    }
    
    // we will create a new group code based on a string like C{20,80}
    private synchronized String getNewGroupCode(String description) throws MFException {
    	// TODO because elements are static this will give a problem after a while ... Memory leak !
    	String newGroupCode=digest(description);
    	
		String mf=description.replaceAll("[\\{\\}]", "");
		if (! groups.containsValue(newGroupCode)) {
			groups.put(newGroupCode, new Group(newGroupCode, description, mf, elements, groups));
		}
		return newGroupCode;
    } 
    
    private String digest(String description) {
    	String code="";
    	try {				
			// we create a hash for the id
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			byte[] digest=algorithm.digest(description.getBytes());
			for (int i=0; i<5; i++) {
				if (i==0) {
					code+=(char)(Math.abs(digest[i]%26)+65);
				} else {
					code+=(char)(Math.abs(digest[i]%26)+97);
				}
			}
    	} catch (NoSuchAlgorithmException e) {throw new RuntimeException (e.toString());}
    	return code;
    }
    
    
}