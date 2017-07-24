package org.chemcalc.core;
import java.util.Stack;
/**
 * Molecular formula parser (for one-part formulae). Called 
 * internally by FormulaPart constructor.
 *
 * @author Michal Krompiec
 * @version 08 Jul 2002
 */
class FormulaPartParser {
/**
 * @param formulaPart <code>FormulaPart</code> to create
 * @param molecularFormula molecular formula
 */
	static void parse(FormulaPart formulaPart, String molecularFormula, boolean expandGroups) throws MFException {
		parse(formulaPart, molecularFormula, 1, expandGroups);
	}
/**
 * @param formulaPart <code>FormulaPart</code> to create
 * @param formulaString molecular formula
 * @param count the number (count) of the current part
 */
    static void parse(FormulaPart formulaPart, String formulaString, double count, boolean expandGroups) throws MFException {
        //TO DO: instead of using charAt(x), use: formula[]=fs.toCharArray(); formula[x];
        formulaString=formulaString.trim();
		formulaString=formulaString.replaceAll("[=]",""); //remove all "double bonds"
		if (formulaString.indexOf(".")!=-1) throw 
			new RuntimeException("Error: the formula \""+formulaString+"\"contains dots (error in parsing)");
		if (formulaString.equals("")) {
			return;
		}
		if (Character.isDigit(formulaString.charAt(0))) {
			throw new RuntimeException("Error: the formula starts with a number");
		}		
		if (formulaString.charAt(0)=='x') formulaString="X"+formulaString.substring(1,formulaString.length());
		Stack afixStack=new Stack(); //stack for parentheses affices
		int curMinAfix=1; //current minimal afix
		int curMaxAfix=1; //current maximal afix
		int tAfix,tMinAfix; //affices just read (temporary)
        int curParMinAfix=1,curParMaxAfix=1, //current and previous parentheses affices 
        	prevParMinAfix=1,prevParMaxAfix=1;
   		String afix; //an afix extracted from the formula
   		String symbol; //a symbol of an element or a group
   		int x=formulaString.length()-1; //the index
   		while (x>=0) {
              if (Character.isDigit(formulaString.charAt(x))) { //get affices and walk through parentheses
     		      int afixEnd=x;
     		      while (x>0 && Character.isDigit(formulaString.charAt(x)) ) --x; 
     			  afix=new String(formulaString.substring(x+1,afixEnd+1));
     			  tAfix=Integer.parseInt(afix);
     			  if (formulaString.charAt(x)=='-') {
     			  	  --x;
     			  	  if (x<0) throw new MFException("Syntax error: the formula starts with '-': "+formulaString);
        			  afixEnd=x;
        			  while (x>0 && Character.isDigit(formulaString.charAt(x)) ) --x; 
					  if (x==0) if (Character.isDigit(formulaString.charAt(x))) --x;
					  String minafix=new String(formulaString.substring(x+1,afixEnd+1));
					  if (minafix.length()==0) {
						  tAfix=Integer.parseInt("-"+afix);
						  tMinAfix=tAfix;
						  // throw new MFException("Syntax error: an atom is directly followed by '-': "+fs);
					  } else {
						  tMinAfix=Integer.parseInt(minafix); //tMinAfix is the minimum afix here
					  }
	 			  } else tMinAfix=tAfix;
     			  
     			  // now check what this afix belongs to 
     			  if (formulaString.charAt(x)==')')  { //close parenthesis
     			      --x;
     			      if (x<0) throw new MFException("Syntax error: the formula starts with ')': "+formulaString);
     			      afixStack.push(new Integer(prevParMinAfix));
	   				  afixStack.push(new Integer(prevParMaxAfix)); 
	   				  prevParMinAfix=curParMinAfix;
	   				  prevParMaxAfix=curParMaxAfix;	   
                      curParMaxAfix*=tAfix;
                      curParMinAfix*=tMinAfix;
      			  } else { //not a parenthesis
      			  	curMinAfix=tMinAfix;
	   				curMaxAfix=tAfix;
      			  }
   			  } else { //not a number
   			      if (formulaString.charAt(x)==']') { //isotopes
                      int symbEnd=x; //end of the symbol, fs.charAt(symbEnd)==']'
      		          --x; 
                      if (x<=1) throw new MFException("Syntax error: bad isotope syntax");
       		          if (!(Character.isLetter(formulaString.charAt(x)))) throw
                         new MFException("Syntax error: bad isotope syntax");
       		          while (x>0 && Character.isLowerCase(formulaString.charAt(x))) --x;
                          if (!Character.isUpperCase(formulaString.charAt(x))) throw
                            new MFException("Syntax error: bad symbol (must start with uppercase)");
                          symbol=formulaString.substring(x,symbEnd);
                          --x; 
                          if (x<0) throw 
                             new MFException("Syntax error: bad isotope syntax");
                          if (!Character.isDigit(formulaString.charAt(x))) throw 
                            new MFException("Syntax error: bad isotope syntax");
                          int afixEnd=x;
                          while (x>0 && formulaString.charAt(x)!='[') --x;
                          int isotopeNumber=0;
                          String nm=formulaString.substring(x+1,afixEnd+1);
                          try {
                          	isotopeNumber=Integer.parseInt(nm);
                          } catch (NumberFormatException nfe) {
                          	throw new MFException("Syntax error: bad isotope number "+nm);
                          }
                          if (formulaString.charAt(x)!='[') throw 
                            new MFException("Isotope syntax error: no opening '['");
                          formulaPart.add(symbol,curMinAfix*curParMinAfix,curMaxAfix*curParMaxAfix,isotopeNumber, expandGroups);
                          curMaxAfix=1;    
                          curMinAfix=1;
                          if (x>0) --x; else return;
                  } else {
                      if (Character.isLetter(formulaString.charAt(x))) {
                        int symbEnd=x;
                        while (x>0 && Character.isLowerCase(formulaString.charAt(x))) --x;
                        if (symbEnd==0 && x==0 && formulaString.charAt(0)=='n') {
                            formulaPart.number=0; //undefined
                            return;
                        }
                        if (!Character.isUpperCase(formulaString.charAt(x))) throw
                            new MFException("Syntax error: the symbol must start with uppercase");
                        symbol=formulaString.substring(x,symbEnd+1);
                        if (symbol.equals("X") && x==0) {
                            formulaPart.number=0; //undefined
                            return;
                        }
                        --x; 
                        formulaPart.add(symbol,curMinAfix*curParMinAfix,curMaxAfix*curParMaxAfix,0,expandGroups);
                        curMaxAfix=1;    
                        curMinAfix=1;
                      } else {
                          while (formulaString.charAt(x)== ')') {// open parenthesis (one or more)
                              --x; 
                              if (x<0) throw 
                                new MFException("Syntax error: the formula starts with ')': "+formulaString);
                              afixStack.push(new Integer(prevParMinAfix));
                              afixStack.push(new Integer(prevParMaxAfix)); 
                              prevParMinAfix=curParMinAfix;
                              prevParMaxAfix=curParMaxAfix;    
                          }
                          if (x<0) break;
                          while (formulaString.charAt(x)=='(') { //close parenthesis (one or more)
                              curParMaxAfix=prevParMaxAfix; 
                              curParMinAfix=prevParMinAfix;       
                              if (afixStack.empty()) {
                                  prevParMaxAfix=1;
                                  prevParMinAfix=1; 
                              } else {
                                  prevParMaxAfix=((Integer)afixStack.pop()).intValue();
                                  prevParMinAfix=((Integer)afixStack.pop()).intValue(); 
                              }
                              --x;
                              if (x<0) break;
                          }
                          if (x<0) break;
                          if (formulaString.charAt(x)=='}') { //combinatorial, allowed only in ChemCalc
                               int combEnd=x;
                               while (x>0 && formulaString.charAt(x)!='{') --x;
                               if (formulaString.charAt(x)!='{') throw 
                                 new MFException("Syntax error (combinatorial): no opening brace");
                               processComb(formulaPart,formulaString.substring(x+1,combEnd),
                                    curMinAfix*curParMinAfix,curMaxAfix*curParMaxAfix);
                               --x;
                          }
                       } //end else (not a letter)
                  } //end else (not isotopes)
              }//end else (not a number)
              if (x<0) break;
              char c=formulaString.charAt(x);
              if (c=='{') throw new MFException("Syntax error (combinatorial): no closing brace");
              if (c=='[') throw new MFException("Syntax error (isotope): no closing bracket");
              if (!Character.isLetterOrDigit(c) && (c!='(') &&(c!='[')&&(c!='{') 
                    && (c!=']') && (c!=')')&& (c!='}')) throw 
                       new MFException("Syntax error: unrecognized character: '"+c+"'");  
        }//end while (main loop)
	}

/** designed for ChemCalc only */	
	private static void processComb(FormulaPart fp, String cs,int minAfix,int maxAfix) 
	       throws MFException {
	   //create a "fake" Atoms object that would do the job
	   if (cs.indexOf(",")==-1) throw new MFException("Syntax error: combinatorial");
	   if (minAfix!=maxAfix) throw 
	       new MFException("Combinatorial range formulae are not allowed");
	   String[] p=cs.split(",");
	   double mass=0;
	   int l=p.length;
	   StringBuffer symbol=new StringBuffer("{");
       for (int i=0; i<l-1; i++) {
           String psym=p[i].trim();
           FormulaPart fpc=new FormulaPart(psym,fp.getElements(),fp.getGroups());
	       symbol.append(fpc.toHtml()+",");
	       mass+=fpc.getMass();
	   }	       
       String psym=p[l-1].trim();
       FormulaPart fpc=new FormulaPart(psym,fp.getElements(),fp.getGroups());
       symbol.append(fpc.toHtml()+"}");
       mass+=fpc.getMass();
	   mass/=(double)l; //mean mass
	   fp.addAtoms(new Atom(symbol.toString(),mass,minAfix,maxAfix));
    }
}