package net.fluance.security.core.model.partner;


public class JwtSpec {

	JwtSpecHeader header;
	JwtSpecPayload payload;
	
	public JwtSpecHeader getHeader() {
		return header;
	}
	
	public void setHeader(JwtSpecHeader header) {
		this.header = header;
	}
	
	public JwtSpecPayload getPayload() {
		return payload;
	}
	
	public void setPayload(JwtSpecPayload payload) {
		this.payload = payload;
	}
	
}
