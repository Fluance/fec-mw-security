package net.fluance.security.core.util;


/**
 * This class stores the response of the KeyCloak Token Endpoint
 *
 */
public class KeyCloakAuthToken {
	
	private String accessToken;
	private Integer expiresIn;
	private Integer refreshExpiresIn;
	private String refreshToken;
	private String tokenType;
	private Integer notBeforePolicy;
	private String sessionState;
	
	public KeyCloakAuthToken(String accessToken, Integer expiresIn, Integer refreshExpiresIn, String refreshToken, String tokenType, Integer notBeforePolicy, String sessionState) {
		super();
		this.accessToken = accessToken;
		this.expiresIn = expiresIn;
		this.refreshExpiresIn = refreshExpiresIn;
		this.refreshToken = refreshToken;
		this.tokenType = tokenType;
		this.notBeforePolicy = notBeforePolicy;
		this.sessionState = sessionState;
	}

	
	public String getAccessToken() {
		return accessToken;
	}

	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	
	public Integer getExpiresIn() {
		return expiresIn;
	}

	
	public void setExpiresIn(Integer expiresIn) {
		this.expiresIn = expiresIn;
	}

	
	public Integer getRefreshExpiresIn() {
		return refreshExpiresIn;
	}

	
	public void setRefreshExpiresIn(Integer refreshExpiresIn) {
		this.refreshExpiresIn = refreshExpiresIn;
	}

	
	public String getRefreshToken() {
		return refreshToken;
	}

	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	
	public String getTokenType() {
		return tokenType;
	}

	
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	
	public Integer getNotBeforePolicy() {
		return notBeforePolicy;
	}

	
	public void setNotBeforePolicy(Integer notBeforePolicy) {
		this.notBeforePolicy = notBeforePolicy;
	}

	
	public String getSessionState() {
		return sessionState;
	}

	
	public void setSessionState(String sessionState) {
		this.sessionState = sessionState;
	}
	
}
