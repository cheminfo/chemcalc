package org.chemcalc.core;
/**
 * An Exception cast if the molecular formula is not valid or if a certain 
 * function cannot be used with the given molecular formula.
 * <code>MFException is cast if:
 * <ul><li> there is a syntax error in the formula
 * <li>a method that is designed for non-<i>range</i> formulae is called for a range 
 * formula (except <code>Atoms.getCount()</code> - in this case a 
 * <code>RuntimeException</code> is cast, because applications that allow range 
 * formulae should never call this function).</ul><br>
 * Generally, this exception is for reporting errors in user's input.
 * <p> <code>MFException</code> should not be cast if: <ul>
 * <li>a method is called with nonsense arguments, but the user's input was valid
 * <li>there is an SQL error
 * <li> any other errors not resulting from user's syntax errors </ul>
 *
 * @author Michal Krompiec
 * @version 05 Jul 2002
 */

public class MFException extends Exception {
/**
 * Creates a new <code>MFException</code> with an error message.
 *
 * @param message error message
 */
 public MFException(String message) { 
    super(message);
 }
}