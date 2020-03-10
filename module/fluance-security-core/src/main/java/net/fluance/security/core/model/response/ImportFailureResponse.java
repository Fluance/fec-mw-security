package net.fluance.security.core.model.response;

/**
 * Response with the username which profiles have not been able to be imported and the reason
 *
 */
public class ImportFailureResponse {

	private String username;
	private String reason;

	public ImportFailureResponse(String username, String reason) {
		super();
		this.username = username;
		this.reason = reason;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
