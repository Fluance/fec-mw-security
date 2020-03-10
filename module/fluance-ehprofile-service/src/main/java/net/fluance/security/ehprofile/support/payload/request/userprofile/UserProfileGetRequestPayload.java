/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.userprofile;

/**
 *
 */
public class UserProfileGetRequestPayload {
	private String username;
	private String domain_id;
	
	
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
	/**
	 * @return the domain_id
	 */
	public String getDomain_id() {
		return domain_id;
	}
	/**
	 * @param domain_id the domain_id to set
	 */
	public void setDomain_id(String domain_id) {
		this.domain_id = domain_id;
	}

	
	
}
