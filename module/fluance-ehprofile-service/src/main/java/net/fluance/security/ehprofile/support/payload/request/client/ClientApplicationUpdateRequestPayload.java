/**
 * 
 */
package net.fluance.security.ehprofile.support.payload.request.client;

/**
 *
 */
public class ClientApplicationUpdateRequestPayload {

	private long id;
	private String client_id;
	private String name;
	private String description;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the clientId
	 */
	public String getClient_id() {
		return client_id;
	}
	
	/**
	 * @param clientId the clientId to set
	 */
	public void setClient_id(String clientId) {
		this.client_id = clientId;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
}
