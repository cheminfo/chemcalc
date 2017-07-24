package org.chemcalc.core.data;

import org.chemcalc.core.Element;
import org.chemcalc.core.Isotope;
import org.chemcalc.core.LoadFromFiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class CreateFiles {

	static String[] KEEP_ISOTOPES={"1-1","1-2","1-3","2-3","2-4","3-6","3-7","4-9","5-10","5-11","6-12","6-13","6-14","7-14","7-15","8-16","8-17","8-18","9-19","10-20","10-21","10-22","11-23","12-24","12-25","12-26","13-27","14-28","14-29","14-30","15-31","16-32","16-33","16-34","16-36","17-35","17-37","18-36","18-38","18-40","19-39","19-40","19-41","20-40","20-42","20-43","20-44","20-46","20-48","21-45","22-46","22-47","22-48","22-49","22-50","23-50","23-51","24-50","24-52","24-53","24-54","25-55","26-54","26-56","26-57","26-58","27-59","28-58","28-60","28-61","28-62","28-64","29-63","29-65","30-64","30-66","30-67","30-68","30-70","31-69","31-71","32-70","32-72","32-73","32-74","32-76","33-75","34-74","34-76","34-77","34-78","34-80","34-82","35-79","35-81","36-78","36-80","36-82","36-83","36-84","36-86","37-85","37-87","38-84","38-86","38-87","38-88","39-89","40-90","40-91","40-92","40-94","40-96","41-93","42-92","42-94","42-95","42-96","42-97","42-98","42-100","43-97","43-98","43-99","44-96","44-98","44-99","44-100","44-101","44-102","44-104","45-103","46-102","46-104","46-105","46-106","46-108","46-110","47-107","47-109","48-106","48-108","48-110","48-111","48-112","48-113","48-114","48-116","49-113","49-115","50-112","50-114","50-115","50-116","50-117","50-118","50-119","50-120","50-122","50-124","51-121","51-123","52-120","52-122","52-123","52-124","52-125","52-126","52-128","52-130","53-127","54-124","54-126","54-128","54-129","54-130","54-131","54-132","54-134","54-136","55-133","56-130","56-132","56-134","56-135","56-136","56-137","56-138","57-138","57-139","58-136","58-138","58-140","58-142","59-141","60-142","60-143","60-144","60-145","60-146","60-148","60-150",
			"61-145","61-147","62-144","62-147","62-148","62-149","62-150","62-152","62-154","63-151","63-153","64-152","64-154","64-155","64-156","64-157","64-158","64-160","65-159","66-156","66-158","66-160","66-161","66-162","66-163","66-164","67-165","68-162","68-164","68-166","68-167","68-168","68-170","69-169","70-168","70-170","70-171","70-172","70-173","70-174","70-176","71-175","71-176","72-174","72-176","72-177","72-178","72-179","72-180","73-180","73-181","74-180","74-182","74-183","74-184","74-186","75-185","75-187","76-184","76-186","76-187","76-188","76-189","76-190","76-192","77-191","77-193","78-190","78-192","78-194","78-195","78-196","78-198","79-197",
			"80-196","80-198","80-199","80-200","80-201","80-202","80-204","81-203","81-205","82-204","82-206","82-207","82-208","83-209","84-209","84-210","85-210","85-211","86-211","86-220","86-222","87-223","88-223","88-224","88-226","88-228","89-227","90-230","90-232","91-231","92-233","92-234","92-235","92-236","92-238","93-236","93-237","94-238","94-239","94-240","94-241","94-242","94-244","95-241","95-243","96-243","96-244","96-245","96-246","96-247","96-248","97-247","97-249","98-249","98-250","98-251","98-252","99-252",
			"100-257","101-258","101-260","102-259","103-262","103-266","104-267","105-268",
			"106-269","106-271","107-270","107-272","108-270","108-277","109-276","109-278",
			"110-281","111-280","111-282","112-285","113-284","113-286","114-289",
			"115-288","115-290","116-293","117-292","117-294","118-294"};

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		List<String> allowed=Arrays.asList(KEEP_ISOTOPES);
		
		DecimalFormat df = new DecimalFormat("#.#####");
		
		HashMap<String,Element> oldElements=new HashMap<String,Element>();
	
		try {
			oldElements = LoadFromFiles.elements("data/atomsName.txt","data/isotope1995.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Class thisClass=new CreateFiles().getClass();
		// TODO Auto-generated method stub
        InputStream in = thisClass.getResourceAsStream("nist201507.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        
        int atomNumber=0;
        int massNumber=0;
        String atomSymbol="";
        double relativeAtomMass=0;
        double isotopicComposition=0;
        double standardAtomWeight=0;

        TreeMap<Integer,Element> elements=new TreeMap<Integer, Element>();
        int isotopeCounter=0;
        
        while ((line=br.readLine()) != null) {
        	if (line.equals("")) {
        		// System.out.print("\""+atomNumber+"-"+massNumber+"\""+",");
        //		if (isotopicComposition>0) {  // we SAVE now ALL the elements (Moss common isotopes)
        		if (allowed.contains(atomNumber+"-"+massNumber)) {
	    			Element element;
	    			if (elements.containsKey(atomNumber)) {
	    				element=elements.get(atomNumber);
	    			} else {
	    				element=new Element();
	    				element.setMass(standardAtomWeight);
	    				element.setMonoisotopicMass(relativeAtomMass);
	    				element.setSymbol(atomSymbol);
	    				if (oldElements.get(atomSymbol)!=null) {
	    					element.setName(oldElements.get(atomSymbol).getName());
	    				}
	    				elements.put(atomNumber, element);
	    			}
	    			if (relativeAtomMass>0) {
	    				Isotope isotope=new Isotope(atomNumber, relativeAtomMass, isotopicComposition, atomSymbol);
	    				isotopeCounter++;
	        			element.getIsotopes().put(isotopeCounter, isotope);
	    			}
        		}
                atomNumber=0;
                atomSymbol="";
                relativeAtomMass=0;
                isotopicComposition=0;
                standardAtomWeight=0;
                massNumber=0;
        	}

        	if (line.startsWith("Atomic Number")) {
        		atomNumber=Integer.parseInt(line.replaceAll("^.*= ", ""));
        	} else if (line.startsWith("Mass Number")) {
        		massNumber=Integer.parseInt(line.replaceAll("^.*= ", ""));
        	} else if (line.startsWith("Atomic Symbol")) {
        		atomSymbol=line.replaceAll("^.*= ", "");
        		if (atomSymbol.equals("Uut")) {
        			atomSymbol="Nh";
        		} else if (atomSymbol.equals("Uup")) {
        			atomSymbol="Mc";
        		} else if (atomSymbol.equals("Uus")) {
        			atomSymbol="Ts";
        		} else if (atomSymbol.equals("Uuo")) {
        			atomSymbol="Og";
        		}
        	} else if (line.startsWith("Relative Atomic Mass")) {
        		try {
        			relativeAtomMass=Double.parseDouble(line.replaceAll("^.*= ", "").replaceAll("[^0-9.]",""));
        		} catch (Exception e) {
            		
        		}
        	} else if (line.startsWith("Isotopic Composition")) {
        		try {
        			isotopicComposition=Double.parseDouble(line.replaceAll("^.*= ", "").replaceAll("\\(.*",""))*100;
        		} catch (Exception e) {
        		}
        	} else if (line.startsWith("Standard Atomic Weight")) {
        		try {
        			// we have 3 cases:
        			// * [14.00643,14.00728]
        			// 20.1797(6)
        			// [209] : specifies the isotope with the longest half-live (none of them are stable)
        			
        			String value=line.replaceAll("^.*= ", "");
        			if (value.indexOf(",")>-1) {
        				// we should make an average
        				double first=Double.parseDouble(value.replaceAll("\\[(.*),(.*)\\]", "$1"));
        				double second=Double.parseDouble(value.replaceAll("\\[(.*),(.*)\\]", "$2"));
        				standardAtomWeight=(first+second)/2;
        			} else if (value.indexOf("[")==0) { // this is the mass of the more stable isotope
        				// in those case we will give the a 100% abundance
        				double moreStable=Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        				if (moreStable==massNumber) {
        					isotopicComposition=100;
        				}
        			} else if (value.indexOf(".")>-1) {
        				standardAtomWeight=Double.parseDouble(value.replaceAll("\\(.*", ""));
        			} else {
        				double moreStable=getMoreStable(atomNumber);
        				if (moreStable==massNumber) {
        					isotopicComposition=100;
        				}
        			}
        		} catch (Exception e) {
        			System.out.println(e.toString());
        		}
        	}	
        }
        br.close();
        in.close();
		
        for (Element element : elements.values()) {
        	element.calculateMass();
        	element.calculateMonoisotopicAndNominalMass();
        }
        
        for (Integer id : elements.keySet()) {
        	Element element=elements.get(id);
        	System.out.println(id+"\t"+element.getSymbol()+"\t"+element.getName()+"\t"+element.getMass());
        }
        
        for (Integer idElement : elements.keySet()) {
        	Element element=elements.get(idElement);
        	for (Integer id : element.getIsotopes().keySet()) {
        		Isotope isotope=element.getIsotopes().get(id);
        		System.out.println(id+"\t"+isotope.mass+"\t"+df.format(isotope.percentage)+"\t"+idElement);
        	}
        }
        
	}
	
	public static int getMoreStable(int elementNumber) {
		switch (elementNumber) {
			case 95:
				return 243;
			case 96:
				return 247;
			case 97:
				return 247;
			case 98:
				return 251;
			case 99:
				return 252;
			case 100:
				return 257;
			case 101:
				return 258;
			case 102:
				return 259;
			case 103:
				return 266;
			case 104:
				return 267;
			case 105:
				return 268;
			case 106:
				return 269;
			case 107:
				return 270;
			case 108:
				return 277;
			case 109:
				return 278;
			case 110:
				return 281;
			case 111:
				return 282;
			case 112:
				return 285;
			case 113:
				return 286;
			case 114:
				return 289;
			case 115:
				return 290;
			case 116:
				return 293;
			case 117:
				return 294;
			case 118:
				return 294;
		}
		return 0;
	}
}

