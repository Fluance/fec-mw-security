package net.fluance.security.core.model.partner;

public class Partner {
	
	private String name;		
	private String sslKeyAlias;
	private JwtSpec jwtSpec;
	private String grantedCompanyCheckUrl;
	
	public JwtSpec getJwtSpec() {
		return jwtSpec;
	}
	
	public void setJwtSpec(JwtSpec jwtSpec) {
		this.jwtSpec = jwtSpec;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSslKeyAlias() {
		return sslKeyAlias;
	}
	
	public void setSslKeyAlias(String sslKeyAlias) {
		this.sslKeyAlias = sslKeyAlias;
	}
	
	public String getGrantedCompanyCheckUrl() {
		return grantedCompanyCheckUrl;
	}
	
	public void setGrantedCompanyCheckUrl(String grantedCompanyCheckUrl) {
		this.grantedCompanyCheckUrl = grantedCompanyCheckUrl;
	}
}
