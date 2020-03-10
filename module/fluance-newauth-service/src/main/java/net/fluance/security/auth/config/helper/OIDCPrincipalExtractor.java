package net.fluance.security.auth.config.helper;

import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;

public class OIDCPrincipalExtractor implements PrincipalExtractor {

	private String defaultDomain;
	
	public OIDCPrincipalExtractor(String defaultDomain) {
		super();
		this.defaultDomain = defaultDomain;
	}
	
	@Override
	public Object extractPrincipal(Map<String, Object> map) {
		return defaultDomain + "/" + map.get("preferred_username");
	}
}
