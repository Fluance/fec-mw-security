package net.fluance.security.core.model.response;

/**
 * Response with the username which profiles have been imported successfully
 *
 */
public class ImportSuccessResponse {

	private String username;

	public ImportSuccessResponse(String username) {
		super();
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	
}
