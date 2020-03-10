/**
 * 
 */
package net.fluance.security.auth.support.payload.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.annotations.ApiModelProperty;

/**
 * Expected body for the payload for the endpoints for creating JWT tokens
 */
public class JWTTokenRequestBody {
	
	private boolean signed;
	private String signingAlgorithm;
	private String type;
	private ObjectNode payload;
	// Custom header fields
	private ObjectNode header;
	
	public boolean isSigned() {
		return signed;
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
	}
	
	@JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(required = true, dataType = "java.lang.String")
	public ObjectNode getPayload() {
		return payload;
	}
	
	public void setPayload(ObjectNode payload) {
		this.payload = payload;
	}

	/**
	 * @return the signingAlgorithm
	 */
	public String getSigningAlgorithm() {
		return signingAlgorithm;
	}

	/**
	 * @param signingAlgorithm the signingAlgorithm to set
	 */
	public void setSigningAlgorithm(String signingAlgorithm) {
		this.signingAlgorithm = signingAlgorithm;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the header
	 */
	@JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(required = true, dataType = "java.lang.String")
    public ObjectNode getHeader() {
		return header;
	}

	/**
	 * @param header the header to set
	 */
	public void setHeader(ObjectNode header) {
		this.header = header;
	}

}
