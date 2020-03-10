package net.fluance.security.core.model.jdbc;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the user_info database table.
 * 
 */
@SuppressWarnings("serial")
public class UserInfo implements Persistable<String> {

	private String id;
	private String userInfo;
	
	public UserInfo() {
	}

	/**
	 * 
	 * @param id
	 * @param userInfo
	 */
	public UserInfo(String id, String info) {
		super();
		this.id = id;
		this.userInfo = info;
	}

	/**
	 * @return the userInfo
	 */
	public String getUserInfo() {
		return userInfo;
	}
	
	/**
	 * @param userInfo the userInfo to set
	 */
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@JsonIgnore
	@Override
	public boolean isNew() {
		// id must be null for a new (unsaved) entity
		// when the id is auto-generated
		return id == null;
	}
	
}