package org.chemcalc.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Contains a group of atoms, for example "Me" or "Ph".
 *
 * @author Michal Krompiec <mkrom@zeus.polsl.gliwice.pl>
 * @version 09 Jul 2002
 */
public class Group {
/** 
 * Returns the symbol of the group. 
 * The symbol is an abbreviation of group's name, for example Me or Ph.
 */    
    public String getSymbol() {
        return groupSymbol;
    }
/** The symbol of the group, for example "Me" */
    private String groupSymbol;
/** definition of this group*/
    private FormulaPart groupMF;

/** 
 * Creates a group. Does not add the group to <code>groups</code>.
 * 
 * @param groupSymbol The symbol of the group, for example "Me"
 * @param groupName The name of the group, for example "Methyl"
 * @param groupFormula The formula of the group, being its definition, for example "CH3"
 * @param elements the elements
 * @param groups other known groups
 * @throws MFException if <code>groupFormula</code> is not a valid molecular formula
 * @throws RuntimeException if other error occured during the creation of the group
 *
 * @see Formula 
 * @see Element
 * @see LoadFromDB#elements
 * @see LoadFromDB#groups
 */ 
    public Group(String groupSymbol,String groupName, String groupFormula, HashMap elements, HashMap groups) 
        throws MFException {
        this.groupSymbol=groupSymbol;
        this.name=groupName;
        try {
            groupMF=new FormulaPart(groupFormula,elements,groups);
        } catch (Exception e) {
            System.out.println("Group creation error: '"+groupSymbol+"'='"+groupFormula
                    +"': "+e.toString());
                    //e.getMessage());
            groupMF=new FormulaPart("",elements,groups);
        }  
        if (groupMF.isRange()) throw new MFException("Range formulas not allowed for groups"); 
    }
    
    
    public double getMonoisotopicMass() throws MFException {
    	return groupMF.getMonoisotopicMass();
    }
    
    public int getNominalMass() throws MFException {
    	return groupMF.getNominalMass();
    }
    
    public double getMass() throws MFException {
    	return groupMF.getMass();
    }
    
  private String name;
/**
 * Returns the name of the group.
 */  
  public String getName() {
      return name;
  }
  
/**
 * Returns the formula of the group.
 */  
  public String getFormula() {
    return groupMF.toString();
  }
  
  /** 
   * Returns the iterator over the <code>Atoms</code> of the group's formula. 
   */ 
    public Iterator<Atom> getIterator() {
       return groupMF.iterator();
    }
    
	public JSONObject toJSON() throws JSONException {
		JSONObject jsonGroup=new JSONObject();
		jsonGroup.put("name", this.name);
		jsonGroup.put("symbol", this.groupSymbol);
		jsonGroup.put("mf", this.groupMF);
		try {
			jsonGroup.put("mass", this.getMass());
		} catch (MFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonGroup;
	}
	
}