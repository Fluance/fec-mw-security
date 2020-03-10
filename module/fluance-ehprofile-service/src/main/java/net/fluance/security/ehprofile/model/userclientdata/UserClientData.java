package net.fluance.security.ehprofile.model.userclientdata;

public class UserClientData {

	private String history;
	private String preferences;
	
	public UserClientData() {}
	
	/**
	 * @param clientId
	 * @param profileId
	 * @param history
	 * @param preferences
	 */
	public UserClientData(String history, String preferences) {
		super();
		this.history = history;
		this.preferences = preferences;
	}
	/**
	 * @return the history
	 */
	public String getHistory() {
		return history;
	}
	/**
	 * @param history the history to set
	 */
	public void setHistory(String history) {
		this.history = history;
	}
	/**
	 * @return the preferences
	 */
	public String getPreferences() {
		return preferences;
	}
	/**
	 * @param preferences the preferences to set
	 */
	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}

}