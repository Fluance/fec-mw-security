package net.fluance.security.ehprofile.support.payload.request.userprofile;

public class UserProfileCreateRequestPayload {
	private String username;
	private String domain_name;


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
	public String getDomain_name() {
		return domain_name;
	}
	/**
	 * @param domain_id the domain_id to set
	 */
	public void setDomain_name(String domain_name) {
		this.domain_name = domain_name;
	}
}
