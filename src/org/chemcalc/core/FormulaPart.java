package org.chemcalc.core;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;


/**
 * A simple one-part chemical formula. That is, a formula like "C2H6", "HAlaOH", "NH3". 
 * It can be (and usually is) a part of a multipart formula. Multipart formulae, 
 * like "NH3.BF3", "CaSO4.2H2O" consist of more than one part (<code>FormulaPart</code>) 
 * and are stored in a higher-level formula container <code>Formula</code>. <p>
 * The formula can be a range formula, that is, the number of atoms can be defined 
 * as being within a range. Example: MeO1-2(CH2)4-6Ph. 
 * This kind of formulae is used in database queries. <p>
 * Atoms in the formula are stored as <code>Atoms</code> objects and are sorted. <p>
 * In most cases it is better to represent a molecular formula as <code>Formula</code>, 
 * not as a <code>FormulaPart</code>, because <code>Formula</code> is more general. 
 *
 * @version 15 Jul 2002
 * @author Michal Krompiec
 * @see Atom
 * @see Formula
 */
public class FormulaPart extends TreeSet<Atom> {

	final static boolean DEBUG=false;
	/** The number of this part, for example 5 in "5H2O" of "CuSO4.5H2O" */
 	double number;
    private HashMap<String,Element> elements;
    private HashMap<String,Group> groups;
	private boolean containsIsotopes=false;
	private boolean combinatorial=false;
	public int charge=0;
	public String comment="";
    
	
/**
 * Creates a simple (one-part) molecular formula. 
 * NH3 is a "simple" formula, NH3.HCl is a "multipart" formula (and cannot be stored in 
 * a <code>FormulaPart</code>.
 * @param formulaString the molecular formula
 * @param elements the elements
 * @param groups atom groups
 * @param number the number of this part in the multipart formula, 
 * for example 5 if the whole formula is CuSO4.5H2O and this part is H2O
 * @see Formula
*/

    public FormulaPart(String formulaString, HashMap elements, HashMap groups, double number, boolean expandGroups)  throws MFException {
        this.elements=elements;
        this.groups=groups;
        this.number=number;
        
        // we check if there is a comment and we remote it
        int pos=formulaString.indexOf("$");
        
        if (pos>-1) {
        	this.comment=formulaString.substring(pos+1);
        	formulaString=formulaString.substring(0, pos);
        }
        
        // Are there any charge in parenthesis ???  (+1)Me2CH(+1)(-1) Lys(+)
        // we need to replace each time we see a charge ...
        
        RegExp pattern;
        
// Strange code, looking for curly brackets ????
        /*
		pattern=RegExp.compile("([A-Z][a-z]?\\{[0-9.,]*\\})");
		for(MatchResult result = pattern.exec(formulaString); result != null; result = pattern.exec(formulaString)) {
			String chargeStr=result.getGroup(0);
			charge+=Integer.parseInt(chargeStr.replaceAll("[^0-9+-]","").replaceAll("-$","-1").replaceAll("[+]$","1").replaceAll("^[+]",""));
			formulaString=pattern.replace(formulaString, "");
		}
		*/
		
		// Change (2+) to Zplus  / Zminus
        
        
        pattern=RegExp.compile("(\\([0-9]+[+-]\\))");
		for(MatchResult result = pattern.exec(formulaString); result != null; result = pattern.exec(formulaString)) {
			String chargeStr=result.getGroup(0);
			int value=Integer.parseInt(chargeStr.replaceAll("[^0-9+-]","").replaceAll("^([0-9]+)([+-])$","$2$1").replaceAll("^[+]",""));
			formulaString=pattern.replace(formulaString, "(Zcharge"+value+")");
		}
		
		
		// Remove (+2)
		pattern=RegExp.compile("(\\([+-][0-9]+\\))");
		for(MatchResult result = pattern.exec(formulaString); result != null; result = pattern.exec(formulaString)) {
			String chargeStr=result.getGroup(0);
			int value=Integer.parseInt(chargeStr.replaceAll("[^0-9+-]","").replaceAll("^([0-9])([+-])$","$1$2").replaceAll("^[+]",""));
			formulaString=pattern.replace(formulaString, "(Zcharge"+value+")");
		}
		
		
		// If we have some + or - not followed by a number it is a charge
		pattern=RegExp.compile("([+-])(?![0-9])");
		for(MatchResult result = pattern.exec(formulaString); result != null; result = pattern.exec(formulaString)) {
			String chargeStr=result.getGroup(0);
			if (chargeStr.equals("+")) {
				formulaString=pattern.replace(formulaString, "Zcharge");
			} else {
				formulaString=pattern.replace(formulaString, "(Zcharge-1)");
			}
		}
		
		if (DEBUG) System.out.println("After charge replacement: "+formulaString);
				
        if (formulaString.matches("[a-zA-Z][0-9]+-[0-9]")) {
        	// System.out.println(frm+" is a RANGE");
        	isRange=true;
        }
        
      //  if (formulaString.indexOf("-")!=-1) isRange=true;
        if (formulaString.indexOf("}")!=-1) combinatorial=true;
        FormulaPartParser.parse(this,formulaString,expandGroups);
    }
    
    
/**
 * Creates a simple (one-part) molecular formula. 
 * NH3 is a "simple" formula, NH3.HCl is a "multipart" formula (and cannot be stored in 
 * a <code>FormulaPart</code>.
 * @param formulaString the molecular formula
 * @param elements the elements
 * @param groups atom groups
 * @see Formula
*/

    public FormulaPart(String formulaString, HashMap elements, HashMap groups) throws MFException {
    	this(formulaString, elements, groups, 1);
    }
   
    public FormulaPart(String formulaString, HashMap elements, HashMap groups, double number) throws MFException {
    	this(formulaString, elements, groups, number, true);
    }

    public FormulaPart(Comparator<Atom> comparator) {
    	super(comparator);
    }
    
/**
 * Returns the number of this part, for example 5 in "5H2O" of "CuSO4.5H2O".
 */
 	public double getNumber() {
 	    return number;
 	}

/**
 * Returns true if the formula is <i>combinatorial</i>. 
 * A formula is combinatorial if it contains a {...} phrase. <br>
 * Example: {Ph,Me)H (means: benzene or methane). 
 * Such formulae work well in ChemCalc only and should never be used for queries.
 */	
	public boolean isCombinatorial() {
	   return combinatorial;
    }
/**
 * Returns true if any of the atoms in this formula is a specified isotope.
 * @return true if for any of the atoms comprising the 
 * molecule the isotope number is specified
 */	
	public boolean containsIsotopes() {
	   return containsIsotopes;
    }

 	void add(String symbol,int minAfix, int maxAfix, int isotopeNumber, boolean expandGroup) throws MFException {

 		if (symbol.equals("Zcharge")) {
 			this.charge+=minAfix;
 			return;
 		}
 		
        if (isotopeNumber!=0) containsIsotopes=true;
        if (symbol.charAt(0)=='[') {
            int cnt=1; 
            while (Character.isDigit(symbol.charAt(cnt))) cnt++;
            if (isotopeNumber==0) {
                try {
                    isotopeNumber=Integer.parseInt(symbol.substring(1,cnt));
                } catch (Exception ne) {
                    throw new MFException("Bad isotope syntax");
                }
            }
            symbol=symbol.substring(cnt,symbol.length()-1);
        }
        Element element=(Element)elements.get(symbol);
        if (element==null) {
            Group group=(Group)groups.get(symbol);
            if (group==null) throw new MFException("Unrecognized symbol: "+symbol);
            if (isotopeNumber!=0) throw 
                    new MFException("Syntax error: cannot specify isotopes for groups");
            
            if (expandGroup) {
                Iterator git=group.getIterator();
                while (git.hasNext()) {
                    Atom gAt=(Atom)git.next();
                    Atom newAt=new Atom(gAt);
                    newAt.minCount*=minAfix;
                    newAt.maxCount*=maxAfix;
                    addAtoms(newAt);
                }
            } else {
            	addAtoms(new Atom(group,minAfix,maxAfix));
            }
        } else {
            addAtoms(new Atom(element,isotopeNumber,minAfix,maxAfix));
        }
        
 	}
 	
/**
 * Adds atoms to the formula.
 * @param at atoms to be added
 * @see Atom
 */
 	public void addAtoms(Atom at) throws MFException{
 	    if (at.maxCount<at.minCount)  throw new MFException("Syntax error: maxCount<minCount");
 	    Atom existing=findAtoms(at.getSymbol());
 	    if (existing!=null) if (existing.getIsotopeNumber()==at.getIsotopeNumber()) {
 	        existing.minCount+=at.minCount;
            existing.maxCount+=at.maxCount;
            // if (existing.maxCount==0 && existing.minCount==0) isRange=true;
            return;
 	    }
        if (at.maxCount==0 && at.minCount==0) isRange=true;
 		this.add(at);
 	}
 	
 /**
 * Remove atoms to the formula.
 * @param at atoms to be added
 * @see Atom
 */
 	public void removeAtoms(Atom at) throws MFException{
 	    if (at.maxCount<at.minCount) 
 	          throw new MFException("Syntax error: maxCount<minCount");
 	    Atom existing=findAtoms(at.getSymbol());
 	    if (existing!=null) if (existing.getIsotopeNumber()==at.getIsotopeNumber()) {
 	        existing.minCount-=at.minCount;
            existing.maxCount-=at.maxCount;
            if (existing.maxCount==0 && existing.minCount==0) this.remove(at);
 	    } else {
	        throw new MFException("Syntax error: negative number of atoms");
	 	}
	 	
	   if ((existing.maxCount<0) || (existing.minCount<0))
          throw new MFException("Syntax error: negative number of atoms");
 	}
 	
 	public void removeMolecularFormula(String molecularFormula) throws MFException {
 		FormulaPart toRemoveMF=new FormulaPart(molecularFormula, elements, groups);
 		Iterator it=toRemoveMF.iterator();
 		Atom at;
 	    while (it.hasNext()) {
 	        at=(Atom)it.next();
 			removeAtoms(at);
 		} 		
 	}
 	
 	
/**
 * Returns Atoms having the given <code>symbol</code> or null if not found. 
 */
 	public Atom findAtoms(String symbol) {
 		for (Atom atom : this) {
 			if (atom.getSymbol().equals(symbol)) return atom;
 		}
 		return null;
 	}
/**
 * Returns Atoms having the given <code>symbol</code> 
 * and <code>isotopeNumber</code> or null if not found.
 */
 
    public Atom findAtoms(String symbol,int isotopeNumber) {
        if (isotopeNumber==0) return findAtoms(symbol);
        for (Atom atom : this) {
            if (atom.getSymbol().equals(symbol) && atom.getIsotopeNumber()==isotopeNumber) return atom;
        }
        return null;
    }

    public Atom findAtom(Atom queryAtom) {
    	for (Atom atom : this) {
            if (atom.equals(queryAtom)) {
            	return atom;
            }
        }
        return null;
    }


 	private boolean isRange=false;
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
 * Returns the formula in text format (without this part's number in the whole formula).
 *
 * @see FormulaPart#toHtml
 * @see Formula#toString
 * @see Atom#toString
 */
 	public String toString() {
 		if (this.size()==0) return "";
    	StringBuffer outStr=new StringBuffer("");
    	for (Atom atom : this) {
    		outStr.append(atom.toString(elements));
    	}
    	// still need to add the charge ...
    	if (charge!=0) {
    		if (charge>0) {
    			outStr.append("(+"+charge+")");
    		} else {
    			outStr.append("("+charge+")");
    		}
    	}
    	
    	return outStr.toString();
 	}
 	
 	
 	public String toHtml() {
 		return toHtml(false);
 	}
 	
/** 
 * Returns the formula as formatted HTML text (with super- and subscripts), 
 * without this part's number in the whole formula.
 *
 * @see FormulaPart#toString
 * @see Formula#toHtml
 * @see Atom#toHtml
 */
 	public String toHtml(boolean utf8) {
        if (this.size()==0) return "";
        StringBuffer outStr=new StringBuffer("");
        for (Atom atom : this) {
        	if (utf8) {
        		outStr.append(atom.toUTF8());
        	} else {
        		outStr.append(atom.toHtml());
        	}
            
        }
        return outStr.toString();
    }
 

 
/**
 * Calculates the mass of the molecule. 
 * Does not take number of this part
 * (see {@link #getNumber FormulaPart.getNumber()}) into account.
 * @return mass in [u]
 * @throws MFException if the formula is a range formula
 * @see Atom#getMass
 * @see Formula#getMass
 * @see #getExactMass
 */
    public double getMass() throws MFException {
        if (isRange) throw new MFException("Cannot calculate mass of a range formula");
        double mass=0;
        for (Atom atom : this) {
            mass+=atom.getMass()*atom.getCount();
        }
        return round6(mass); 
    }
    
/**
 * Calculates the monoisotopic mass of the formula part. 
 * Does not take number of this part
 * (see {@link #getNumber FormulaPart.getNumber()}) into account.
 * @return monoisotopic mass in [u]
 * @throws MFException if the formula is a range formula
 * @see Atom#getMonoisotopicMass
 * @see Formula#getMonoisotopicMass
 * @see #getMass
 */
    public double getMonoisotopicMass() throws MFException {
        if (isRange) throw new MFException("Cannot calculate exact mass of a range formula");
        double exactMass=0;
        for (Atom at : this) {
        	exactMass+=at.getMonoisotopicMass()*at.getCount();
        }
        return round10(exactMass);
    }
    
    public int getNominalMass() throws MFException {
        if (isRange) throw new MFException("Cannot calculate exact mass of a range formula");
        int nominalMass=0;
        for (Atom at : this) {
        	nominalMass+=at.getNominalMass()*at.getCount();
        }
        return nominalMass;
    }

   		
   		
   		
    
    
/**
 * Returns the total number of atoms in this formula. 
 * It is not the number of <code>Atoms</code> objects in the formula.    
 * @throws MFException if the formula is a range formula
 */
    public int getNumberOfAtoms() throws MFException {
        if (isRange) throw 
            new MFException("Cannot get the number of atoms: this is a range formula");
        int num=0;
        for (Atom atom : this) {
            num+=atom.getCount();
        }
        return num;
    }
    
    private double round6(double what) {
        double r=Math.rint(what*1e6);
        return r/1e6;
    }  
    
    public static double round10(double what) {
        double r=Math.rint(what*1e10);
        return r/1e10;
    }    

/**
 * Calculates the elemental composition (in mass percent). 
 * The results are stored in <code>Atoms</code> object of the formula.
 * @throws MFException if this formula is a range formula
 * @see Atom
 */
    public void calculateElementalAnalysis() throws MFException {
        double mass=getMass();
        for (Atom atom : this) {
            atom.percentage=round6(atom.getMass()*atom.getCount()*100/mass);
        }
    }
    
/**
 * Removes all the atoms that have zero afix.
 * If a formula contains atoms with zero affices,
 * it is treated as a <i>range formula</i> and
 * <code>isRange()</code> would return <code>true</code>.
 *
 * @see FormulaPart#removeZeroes()
 * @see #isRange()
 */
    public void removeZeroes() {
        if (!isRange()) return;
        for (Iterator it=this.iterator();it.hasNext();) {
            Atom at=(Atom)it.next();
            if (at.minCount==0 && at.maxCount==0) it.remove();
        }
       boolean range=false;
       for (Atom atom : this) {
            if (atom.minCount!=atom.maxCount) range=true;
        }
		isRange=range;
    }
    
    HashMap getElements() {
        return elements;
    }
    
    HashMap getGroups() {
        return groups;
    }
    
    public final static void main(String[] args) {
    	try {
			new FormulaPart("(+2)Ala(-1)(+1)",null,null,1);
		} catch (MFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
}
