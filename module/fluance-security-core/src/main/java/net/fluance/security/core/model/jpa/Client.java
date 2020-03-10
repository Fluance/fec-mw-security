package net.fluance.security.core.model.jpa;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;


/**
 * The persistent class for the client database table.
 * 
 */
@JsonIgnoreProperties(value={"version"}, ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name="client")
public class Client extends JPABaseEntity {

	@Id
	@Column(unique=true, nullable=false)
	@org.hibernate.annotations.Type(type="org.hibernate.type.PostgresUUIDType")
	private UUID id;

	private String description;

	@Column(nullable=false)
	private String name;

	@Column(nullable=false)
	private String secret;

	public Client() {
	}

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public String getSecret() {
		return this.secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public Integer getVersion() {
		return null;
	}

}