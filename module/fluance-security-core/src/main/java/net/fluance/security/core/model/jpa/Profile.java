package net.fluance.security.core.model.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;

/**
 * The persistent class for the company database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "profile")
public class Profile extends JPABaseEntity {

	@Id
	protected Integer id;
	private String username;
	@Column(name = "domainname")
	private String domainName;

	@Column(name = "language")
	private String language;
	//bi-directional many-to-many association to Role
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
		name="profile_role"
		, joinColumns={
			@JoinColumn(name="profile_id", nullable=false)
			}
		, inverseJoinColumns={
			@JoinColumn(name="role_id", nullable=false)
			}
		)
	List<Role> roles;
	@ManyToOne
	@JoinColumn(name="usertype_id")
	private UserType userType;
	
	public Profile() {
		roles = new ArrayList<>();
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}


	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	public String getDomainName() {
		return domainName;
	}
	
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	@Override
	public Integer getVersion() {
		return null;
	}

	@Override
	public Integer getId() {
		return id;
	}
	
	public void assignRole(Role role) {
		roles.add(role);
	}

	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	
}