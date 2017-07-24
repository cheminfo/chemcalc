package org.chemcalc.core;



public class ExtendedAtom implements Comparable<ExtendedAtom> {
	final static boolean DEBUG=false;
	final static boolean OPTIMIZE=true;

	
	public int currentCount;
	public int currentMinCount; // each Atom object contains a minCount and maxCount that may differ from the practically possible range
	public int currentMaxCount;
	public Atom atom;

	int getRangeSize() {
		return this.atom.maxCount-this.atom.minCount;
	}
	
	public void setCurrentMinMax(double currentMass, double targetMass, double minInnerMass, double maxInnerMass, double massRange) {
		if (! OPTIMIZE) {
			this.currentMinCount=this.atom.minCount;
			this.currentMaxCount=this.atom.maxCount;
			this.currentCount=this.atom.minCount-1;
		} else {
			if (DEBUG) System.out.println("Evaluating range for: "+this.atom.getSymbol()+" - "+currentMass+" - "+targetMass+" - "+minInnerMass+" - "+maxInnerMass);
			this.currentMinCount=Math.max((int)Math.floor((targetMass-massRange-currentMass-maxInnerMass)/this.atom.getMonoisotopicMass()),this.atom.minCount);
			this.currentMaxCount=Math.min((int)Math.ceil((targetMass+massRange-currentMass-minInnerMass)/this.atom.getMonoisotopicMass()),this.atom.maxCount);
			this.currentCount=this.currentMinCount-1;
			if (DEBUG) System.out.println("Result: "+currentMass+" - Min:"+this.currentMinCount+" - Max:"+this.currentMaxCount);
		}
	}
	
	
	public ExtendedAtom (Atom atom) throws MFException {
		this.atom=atom;
		if (atom.minCount>atom.maxCount) throw new MFException("Error: minCount>maxCount");
	}
	
	public double getMinMass() {
		return atom.getMonoisotopicMass()*atom.minCount;
	}
	
	public double getMaxMass() {
		return atom.getMonoisotopicMass()*atom.maxCount;
	}
	
	public String toString() {
		return atom.getSymbol()+" - "+atom.minCount+" to "+atom.maxCount+" - current: "+currentCount;
	}
	
	public int compareTo(ExtendedAtom otherObject) {
		// return new Double(this.atom.getMonoisotopicMass()).compareTo(otherObject.atom.getMonoisotopicMass())*-1;
		return new Integer(this.getRangeSize()).compareTo(otherObject.getRangeSize());
		// return 0;
	}
}
