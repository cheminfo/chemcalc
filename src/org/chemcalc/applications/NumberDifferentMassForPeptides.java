package org.chemcalc.applications;


import org.chemcalc.core.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

public class NumberDifferentMassForPeptides {
	final static boolean DEBUG=true;
	HashMap<String,Element> elements=new HashMap<String,Element>();
	HashMap<String,Group> groups=new HashMap<String,Group>();
	HashMap<String,String> options=new HashMap<String,String>();
	private double resolution=10000;
	boolean init=false;
	private void init() throws MFException {
		if (init) return;

		try {
			elements = LoadFromFiles.elements("data/atom1995.txt","data/isotope1995.txt");
			groups=LoadFromFiles.groups("data/group.txt",elements);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		init=true;
	}
	
	private void execute() throws MFException {
		TreeSet<Integer> result=new TreeSet<Integer>();
		this.init();
		
		String[] allowed={"Ala","Arg","Asn","Asp","Cys","Gln","Glu","Gly","His","Ile","Leu","Lys","Met","Phe","Pro","Ser","Val","Thr","Trp","Tyr"};
		double[] masses=new double[allowed.length];
		for (int i=0; i<allowed.length; i++) {
			String mf=allowed[i];
			masses[i]=new Formula(mf, elements, groups).getMonoisotopicMass();
		}
		
		int nbAA=allowed.length;
		
		nbAA=allowed.length-1;
		
		int seqLength=6;
		int[] currentPositions=new int[seqLength];
		for (int i=0; i<seqLength; i++) {
			currentPositions[i]=-1;
		}
		int counter=0;
		int current=0;
		double mass=0;
		
		while (current>=0) {
			if (current<seqLength) {
				if (currentPositions[current]<nbAA) {
					currentPositions[current]++;
					mass+=masses[currentPositions[current]];
					current++;					
				} else {
					currentPositions[current]=-1;
					current--;
					if (current>=0) {
						mass-=masses[currentPositions[current]];
					}
				}
			} else {
				counter++;
				print(currentPositions,mass);
				result.add((int)(mass*resolution));
				current--;
				mass-=masses[currentPositions[current]];
			}
		}
	/*	for (Integer value : result) {
			System.out.println(value/resolution);
		}
		*/
		System.out.println("Number found: "+result.size());
		System.out.println("Number possibilities checked: "+counter);	
		
	}
	
	private void print(int[] values, double mass) {
		String string="";
		for (int i=0; i<values.length; i++) {
			string+=values[i]+" ";
		}
	//	System.out.println(string+" = "+mass);
	}
	
	public static void main(String[] args) {
		try {
			new NumberDifferentMassForPeptides().execute();
		} catch (MFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
