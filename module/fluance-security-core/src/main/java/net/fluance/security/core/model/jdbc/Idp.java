package net.fluance.security.core.model.jdbc;

import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the user_info database table.
 * 
 */
@SuppressWarnings("serial")
public class Idp implements Persistable<Integer> {

	private Integer id;
	private String displayName;
	private String url;
	private String imgUrl;
	
	public Idp(Integer id, String displayName, String url, String imgUrl) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.url = url;
		this.imgUrl = imgUrl;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	@JsonIgnore
	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}
}