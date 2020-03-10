package net.fluance.security.core.model.partner;


public class JwtSpecHeader {
	private String signingAlgorithm;
	private String type;
	
	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}
	
	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	
}
