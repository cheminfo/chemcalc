package org.chemcalc.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Two static functions to load chemical elements and groups of atoms from the database.
 * Will be replaced by a call to <bold>dbcreator</bold>.
 *
 * @author Michal Krompiec
 * @version 05 Jul 2002
 */    
public class LoadFromFiles {
	// there could be unlimited number of preferences

	private static HashMap<String,HashMap<String,Group>> allGroups=new  HashMap<String,HashMap<String,Group>>();
	private static HashMap<String,HashMap<String,Element>> allElements=new HashMap<String,HashMap<String,Element>>();

	public synchronized static HashMap<String,Element> elements(String year) throws IOException {
		   return elements("data/atom"+year+".txt","data/isotope"+year+".txt");
	}
	   
	/**
	 * Loads the elements from the file.
	 */
    public synchronized static HashMap<String,Element> elements(String atomFilename, String isotopeFilename) throws IOException {
    	String key=atomFilename+isotopeFilename;
    	if (allElements.containsKey(key)) {
    		return allElements.get(key);
    	}
        HashMap<String,Element> elements=new HashMap<String,Element>();
        Class thisClass=new LoadFromFiles().getClass();
        
        HashMap <String,Element> elementIds=new HashMap<String,Element>();
        
        InputStream in = thisClass.getResourceAsStream(atomFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line=br.readLine()) != null) {
        	String [] values=line.split("\t");
        	Element element=new Element(Double.parseDouble(values[3]), values[1].trim(), values[2], Integer.parseInt(values[0]));
        	elements.put(element.getSymbol(),element);
        	elementIds.put(values[0], element);
        }
        br.close();
        in.close();
        
        in = thisClass.getResourceAsStream(isotopeFilename);
        br = new BufferedReader(new InputStreamReader(in));
        while ((line=br.readLine()) != null) {
        	String [] values=line.split("\t");
        	Element element=elementIds.get(values[3]);
        	double isotopeMass=Double.parseDouble(values[1]);
        	double isotopePercent=Double.parseDouble(values[2]);
        	Isotope Is=new Isotope((int)(isotopeMass+0.5),isotopeMass,isotopePercent,element.getSymbol());
        	element.addIsotope(Is);
        }
        br.close();
        in.close();       
        
    
        for (Element element : elements.values()) {
            element.calculateMonoisotopicAndNominalMass();
            if (element.getNumIsotopes()==0) {
                Isotope singleIs=new Isotope((int)(element.getMass()+0.5),element.getMass(),100,element.getSymbol());
                element.addIsotope(singleIs);
            }
        }

        allElements.put(key, elements);
        return elements;
    }
    
/**
 * Loads the groups from the database.
*/

    public synchronized static HashMap<String,Group> groups(String groupFilename, HashMap<String,Element> elements)  throws IOException,MFException {
             //load the groups
       	String key=groupFilename;
    	if (allGroups.containsKey(key)) {
    		return allGroups.get(key);
    	}

    	 HashMap<String,Group> groups=new HashMap<String,Group>();
        
        Class thisClass=new LoadFromFiles().getClass();
          
        InputStream in = thisClass.getResourceAsStream(groupFilename);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line=br.readLine()) != null) {
        	String [] values=line.split("\t");
        	groups.put(values[1], new Group(values[1],values[2],values[3],elements,groups));
        }
        br.close();
        in.close();

        allGroups.put(key,groups);
        return groups;
    }
    
    
    public static void main(String[] args) {
    	
    	try {
    		HashMap<String,Element> elements=LoadFromFiles.elements("data/atom1995.txt","data/isotope1995.txt");
    		HashMap<String,Group> groups=LoadFromFiles.groups("data/group.txt",elements);
		
    		for (Element element : elements.values()) {
    			System.out.println(element.toString());
    		}
    		
    		for (Group group : groups.values()) {
    			System.out.println(group.toString());
    		}
    		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MFException mfe) {
			mfe.printStackTrace();
		}
    }
    
}