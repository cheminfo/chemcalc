package org.chemcalc.core;

import java.util.HashMap;

/**
 * One or more atoms of the same element or the same isotope 
 * in a molecular formula.
 * The number of atoms can be a single number (like C<sub>2</sub>, 
 * two carbon atoms) or a range (C<sub>2-4</sub>, from two to four 
 * carbon atoms - this is used mostly for database queries).
 *
 * @author Michal Krompiec
 * @version 15 Jul 2002
 *
 * @see FormulaPart
 * @see Element
 * @see Formula
 */ 
 public class Atom implements Comparable {

	 private int nominalMass;
	 
/** 
 * Creates atoms of element <code>sourceElement</code>.
 * Examples: <ul><li>C<sub>2</sub>: <code>minCount</code>=<code>maxCount</code>=2
 * <li>C<sub>2-4</sub>: <code>minCount=2</code>; <code>maxCount</code>=4 </ul>
 *
 *
 * @param sourceElement the chemical element
 * @param minCount minimal number of atoms
 * @param maxCount maximal number of atoms
 * @throws MFException if minCount>maxCount
 */
    public Atom(Element sourceElement, int minCount, int maxCount) 
            throws MFException {
 		if (minCount>maxCount) throw new MFException("Error: minCount>maxCount");
 		this.minCount=minCount;
 		this.maxCount=maxCount;
 		this.monoisotopicMass=sourceElement.getMonoisotopicMass();
 		this.nominalMass=sourceElement.getNominalMass();
 		this.isotopeNumber=0;
        this.symbol=sourceElement.getSymbol();
        this.mass=sourceElement.getMass();
 	}
 	
    public int getNominalMass() {
	return nominalMass;
	}
	
	public void setNominalMass(int nominalMass) {
		this.nominalMass = nominalMass;
	}

	public Atom(Group group, int minCount, int maxCount)  throws MFException {
		if (minCount>maxCount) throw new MFException("Error: minCount>maxCount");
		this.minCount=minCount;
		this.maxCount=maxCount;
		this.monoisotopicMass=group.getMonoisotopicMass();
		this.nominalMass=group.getNominalMass();
		this.isotopeNumber=0;
		this.symbol=group.getSymbol();
		this.mass=group.getMass();
	}
    
/** 
 * Creates atoms of element <code>sourceElement</code>, 
 * isotope <code>isotopeNum</code>.
 * Examples: <ul><li>D<sub>2</sub>: <code>minCount</code>=<code>maxCount</code>=2; 
 * <code>isotopeNum</code>=2
 * <li>D<sub>2-4</sub>: <code>minCount=2</code>; <code>maxCount</code>=4;
 * <code>isotopeNum</code>=2</ul>
 * <li>H<sub>2</sub>: <code>minCount</code>=<code>maxCount</code>=2; 
 * <code>isotopeNum</code>=0
 * <li>H<sub>2-4</sub>: <code>minCount=2</code>; <code>maxCount</code>=4;
 * <code>isotopeNum</code>=0</ul>
 *
 * @param sourceElement the chemical element
 * @param isotopeNum isotope number (mass number of the isotope) or 0 if isotope not specified
 * @param minCount minimal number of atoms
 * @param maxCount maximal number of atoms
 *
 * @throws MFException if the isotope cannot be found or 
 * if minCount>maxCount
 */
    
    public Atom(Element sourceElement, int isotopeNum, int minCount, int maxCount) 
            throws MFException {
        if (minCount>maxCount) throw new MFException("Error: minCount>maxCount");
 		if (isotopeNum==0) {
            this.minCount=minCount;
            this.maxCount=maxCount;
            this.monoisotopicMass=sourceElement.getMonoisotopicMass();
            this.nominalMass=sourceElement.getNominalMass();
            this.isotopeNumber=0;
            this.symbol=sourceElement.getSymbol();
            this.mass=sourceElement.getMass();
            return;
        }
 		Isotope is=sourceElement.getIsotope(isotopeNum);
 		if (is==null) throw new MFException("Isotope not found");
 		this.isotopeNumber=isotopeNum;
 		this.symbol=sourceElement.getSymbol();
 		mass=is.mass;
 		monoisotopicMass=is.mass;
        this.minCount=minCount;
        this.maxCount=maxCount;
 	}
/** 
 * Creates atoms. Exact mass is set to be equal to atomic mass.
 *
 * @param symbol atomic symbol
 * @param mass atomic mass
 * @param minCount minimal number of atoms
 * @param maxCount maximal number of atoms
 * @throws MFException if minCount>maxCount
 */
    
    public Atom(String symbol,double mass, int minCount, int maxCount) throws MFException{
        if (minCount>maxCount) throw new MFException("Error: minCount>maxCount");
 		this.symbol=symbol; 
 		this.mass=mass; 
        this.minCount=minCount;
        this.maxCount=maxCount;
 	}
 	
    public Atom(Atom atom) {
    	this.symbol=atom.symbol;
    	this.mass=atom.mass;
    	this.minCount=atom.minCount;
    	this.maxCount=atom.maxCount;
    	this.monoisotopicMass=atom.monoisotopicMass;
    	this.nominalMass=atom.nominalMass;
    	this.isotopeNumber=atom.isotopeNumber;
    }
    
/** 
 * Returns the atomic symbol. 
 */
    public String getSymbol() {
        return symbol;
    }
    public String symbol;
    
/** 
 * Returns the atomic symbol with possibly the isotope. 
 */
    public String getFullSymbol() {
		if (isotopeNumber!=0) return "["+isotopeNumber+symbol+"]";
        else return symbol;        
	}
    
/** 
 * Returns the mass number if a nuclid, otherwise returns 0. 
 */
    public int getIsotopeNumber() {
        return isotopeNumber;
    }
    private int isotopeNumber;
/** 
 * Returns the atomic mass (per 1 atom). 
 *
 * @return atomic mass in [u]
 *
 * @see #getExactMass
 * @see FormulaPart#getMass
 * @see Formula#getMass
 */
    public double getMass() {
        return mass;
    }
    private double mass;
    
/** 
 * Percentage in elemental composition. 
 * Calculated by an external function: FormulaPart.calculateElementalAnalysis()
 *
 * @see FormulaPart#calculateElementalAnalysis()
 */
    public double getPercentage(){
        return percentage;
    }
    double percentage;
    
/** 
 * Minimal number of atoms (minimal afix). If minCount is not equal to maxCount, the 
 * fomula is a <i>range</i> formula, used in queries. If minCount = maxCount the formula is a 
 * normal formula. 
 *
 * @see #maxCount
 * @see #getCount
 * @see FormulaPart
 * @see Formula
 */
    public int minCount;
    
/** 
 * Maximal number of atoms (maximal afix). If minCount is not equal to maxCount, the 
 * fomula is a <i>range</i> formula, used in queries. If minCount = maxCount the formula is a 
 * normal formula. 
 *
 * @see #minCount
 * @see #getCount
 * @see FormulaPart
 * @see Formula
 */
    public int maxCount;
    
/** 
 * Returns the number of atoms (the afix).
 * @return the number of atoms if minCount = maxCount, otherwise throws an exception
 *
 * @throws RuntimeException if minCount!=maxCount. This means that this function can be used 
 * only of it is guaranteed that the formula is not a <i>range</i> formula. Applications 
 * that use <i>range</i> formulae should never call this function.
 *
 * @see #minCount
 * @see #maxCount
 * @see FormulaPart
 * @see Formula
 */    
    public int getCount() {
        if (minCount==maxCount) return minCount;
        throw new RuntimeException("Atoms.getCount(): undefined (min!=max)");
    }
    
/** 
 * Returns the monoisotopic mass (the mass of the more abundant isotope) of one atom.
 *
 * @return monoisotopic mass (mass of the more abudant isotope) in [u]
 * @see #getMass
 * @see FormulaPart#getExactMass
 * @see Formula#getExactMass
 */ 
    public double getMonoisotopicMass() {
        return monoisotopicMass;
    }
    private double monoisotopicMass;
/** 
 * Returns a string representation of this object.
 *
 * @return the atomic symbol (if a nuclid - in brackets and 
 * preceded by the isotope number) followed by the afix. 
 * Examples: "C", "[17O]", "H4" 
 */
    public String toString(HashMap<String,Element> elements) {
    	String currentSymbol=symbol;
		if (elements!=null && elements.containsKey(symbol) && elements.get(symbol).getName().matches("^[A-Z][a-z]?\\{.*\\}$")) {
			currentSymbol=elements.get(symbol).getName();
		}
        String outs="";
        if (isotopeNumber!=0) {
        	outs+="["+isotopeNumber+currentSymbol+"]";
        } else {
        	outs+=currentSymbol;
        }
        if (maxCount==1 && minCount==1) return outs;
        if (maxCount==minCount) return outs+maxCount;
        return outs+minCount+"-"+maxCount;
    }

    public String toString() {
    	return toString(null);
    }
    
    
    
/** 
 * Returns this atom as formatted HTML text. Isotope number 
 * is in superscript and the afix in subscript. Examples: 
 * [13C]2 is rendered as <sup>13</sup>C<sub>2</sub>, H3-6 as 
 * H<sub>3-6</sub>.
 */
    public String toHtml() {
        String outs="";
        if (isotopeNumber!=0) outs="<sup>"+isotopeNumber+"</sup>"+symbol;
            else outs=symbol;
        if (maxCount==1 && minCount==1) return outs;
        if (maxCount==minCount) return outs+"<sub>"+maxCount+"</sub>";
        return outs+"<sub>"+minCount+"-"+maxCount+"</sub>";
    }

    public String toUTF8() {
        String outs="";
        if (isotopeNumber!=0) outs=superscript(isotopeNumber+"")+symbol;
            else outs=symbol;
        if (maxCount==1 && minCount==1) return outs;
        if (maxCount==minCount) return outs+subscript(maxCount+"");
        return outs+subscript(minCount+"")+"-"+subscript(maxCount+"");
    }
    
    public static String superscript(String str) {
        str = str.replaceAll("0", "⁰");
        str = str.replaceAll("1", "¹");
        str = str.replaceAll("2", "²");
        str = str.replaceAll("3", "³");
        str = str.replaceAll("4", "⁴");
        str = str.replaceAll("5", "⁵");
        str = str.replaceAll("6", "⁶");
        str = str.replaceAll("7", "⁷");
        str = str.replaceAll("8", "⁸");
        str = str.replaceAll("9", "⁹");         
        return str;
    }

    public static String subscript(String str) {
        str = str.replaceAll("0", "₀");
        str = str.replaceAll("1", "₁");
        str = str.replaceAll("2", "₂");
        str = str.replaceAll("3", "₃");
        str = str.replaceAll("4", "₄");
        str = str.replaceAll("5", "₅");
        str = str.replaceAll("6", "₆");
        str = str.replaceAll("7", "₇");
        str = str.replaceAll("8", "₈");
        str = str.replaceAll("9", "₉");
        return str;
    }
    
/** 
 * Returns true if and only if the atomic symbols and the isotope numbers are equal.
 */    
    public boolean equals(Object o) {
        if (o instanceof Atom) {
            Atom at=(Atom)o;
            if (symbol.equals(at.symbol) && isotopeNumber==at.isotopeNumber) return true;
        } else {
        	System.out.println("Atom: Wrong class: "+o);
        }
        return false;
    }
/**
 * Returns the hash code.
 */
    public int hashCode() {
        String idString=isotopeNumber+symbol;
        return idString.hashCode();
    }
/** 
 * Compares two <code>Atoms</code> objects.
 * C is always first, H is after C, other elements are sorted alphabetically. 
 * Nuclids are after natural elements, for example <sup>13</sup>C is after C. The 
 * affices (numbers of atoms, <code>minCount</code> <code>and maxCount</code>) 
 * are not compared.
 */    
    public int compareTo(Object o) {
        if (!(o instanceof Atom)) throw new 
            UnsupportedOperationException("Not comparable: Atoms and "+o.getClass().getName());
        return compareTo((Atom)o);
    }
/** 
* Compares two <code>Atoms</code> objects.
 * C is always first, H is after C, other elements are sorted alphabetically. 
 * Nuclids are after natural elements, for example <sup>13</sup>C is after C. The 
 * affices (numbers of atoms, <code>minCount</code> <code>and maxCount</code>) 
 * are not compared.
 */    
    public int compareTo(Atom at) {
        if (this.equals(at)) return 0;
        if (this.symbol.equals("C")) {
            if (at.symbol.equals("C")) return (this.isotopeNumber-at.isotopeNumber);
            return -1;
        }
        if (this.symbol.equals("H")) {
            if (at.symbol.equals("C")) return 1;
            if (at.symbol.equals("H")) return (this.isotopeNumber-at.isotopeNumber);
            return -1;
        }
        if (at.symbol.equals("H") || at.symbol.equals("C")) {
        	return 1;
        }
        if (this.symbol.equals(at.symbol)) return (this.isotopeNumber-at.isotopeNumber);
        
        return this.symbol.compareTo(at.symbol);
    }
}
