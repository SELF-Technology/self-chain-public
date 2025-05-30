/**
 * 
 */
package org.self.selfscript.exceptions;

/**
 * Exception class for SELFScript operations
 */
public class SelfException extends Exception {
    /**
     * Constructor with error message
     * @param zError Error message
     */
    public SelfException(String zError) {
        super(zError);
    }
}
