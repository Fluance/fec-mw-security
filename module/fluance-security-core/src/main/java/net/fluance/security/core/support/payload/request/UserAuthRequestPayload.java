/**
 * 
 */
package net.fluance.security.core.support.payload.request;

public class UserAuthRequestPayload {

	private String grant_type;
	private String password;
	private String username;
	
	/**
	 * @return the grant_type
	 */
	public String getGrant_type() {
		return grant_type;
	}
	
	/**
	 * @param grant_type the grant_type to set
	 */
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
}
