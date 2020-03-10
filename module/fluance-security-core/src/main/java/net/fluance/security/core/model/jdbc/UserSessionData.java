package net.fluance.security.core.model.jdbc;

import java.util.Date;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.fluance.commons.json.JsonDateTimeSerializer;

/**
 * The persistent class for the user session data database table.
 * 
 */
@SuppressWarnings("serial")
public class UserSessionData implements Persistable<String> {
	
	private String id;
	private String issuer;
	private String subjectId;
	private String sessionIndex;
	private String agent;
	private String ipAddress;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	private Date creationDate;
	@JsonSerialize(using = JsonDateTimeSerializer.class)
	private Date expirationDate;

	/**
	 * 
	 * @param id
	 * @param issuer
	 * @param nameId
	 * @param sessionIndex
	 */
	public UserSessionData(String id, String issuer, String subjectId, String sessionIndex, String agent, String ipAddress, Date creationDate, Date expirationDate) {
		super();
		this.id = id;
		this.issuer = issuer;
		this.subjectId = subjectId;
		this.sessionIndex = sessionIndex;
		this.agent = agent;
		this.ipAddress = ipAddress;
		this.creationDate = creationDate;
		this.expirationDate = expirationDate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonIgnore
	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getSubjectId() {
		return subjectId;
	}
	
	@JsonIgnore
	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	@JsonIgnore
	public String getSessionIndex() {
		return sessionIndex;
	}

	public void setSessionIndex(String sessionIndex) {
		this.sessionIndex = sessionIndex;
	}
	
	public String getAgent() {
		return agent;
	}
	
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getExpirationDate() {
		return expirationDate;
	}
	
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public UserSessionData() {
	}


	@JsonIgnore
	@Override
	public boolean isNew() {
		// id must be null for a new (unsaved) entity
		// when the id is auto-generated
		return id == null;
	}

}