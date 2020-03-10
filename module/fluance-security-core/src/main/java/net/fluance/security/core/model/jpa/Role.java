package net.fluance.security.core.model.jpa;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;

import java.util.List;


/**
 * The persistent class for the client database table.
 * 
 */
@JsonIgnoreProperties(value={"version"}, ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name="role")
public class Role extends JPABaseEntity {

	@Id
	@SequenceGenerator(name="roleIdSeqGenerator", sequenceName="role_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="roleIdSeqGenerator")
	protected Integer id;
	
	private String description;

	@Column(nullable=false, unique = true)
	private String name;
	
	@JsonIgnore
	@ManyToMany(mappedBy="roles", fetch = FetchType.LAZY)
	private List<Profile> users;
	
	public Role() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the users
	 */
	public List<Profile> getUsers() {
		return users;
	}

	/**
	 * @param users the users to set
	 */
	public void setUsers(List<Profile> users) {
		this.users = users;
	}

	@Override
	public Integer getVersion() {
		return null;
	}

	@Override
	public Integer getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Role)) {
			return false;
		}
		Role other = (Role) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	
}