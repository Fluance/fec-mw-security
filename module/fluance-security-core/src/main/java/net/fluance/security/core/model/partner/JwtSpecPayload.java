package net.fluance.security.core.model.partner;

import java.util.List;

public class JwtSpecPayload {

	private Integer validityPeriod;
	private List<String> dynamicClaims;
	
	public Integer getValidityPeriod() {
		return validityPeriod;
	}
	
	public void setValidityPeriod(Integer validityPeriod) {
		this.validityPeriod = validityPeriod;
	}
	
	public List<String> getDynamicClaims() {
		return dynamicClaims;
	}
	
	public void setDynamicClaims(List<String> dynamicClaims) {
		this.dynamicClaims = dynamicClaims;
	}
}
