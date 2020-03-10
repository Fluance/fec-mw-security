/**
 * 
 */
package net.fluance.security.auth.config.helper.jwt.exception;

/**
 *
 */
@SuppressWarnings("serial")
public class InvalidAssertionException extends Exception{

	public InvalidAssertionException(String msg) {
		super(msg);
	}
	
}
