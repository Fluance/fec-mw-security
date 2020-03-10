package net.fluance.security.core.model.jpa;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;


/**
 * The persistent class for the client database table.
 * 
 */
@JsonIgnoreProperties(value={"version"}, ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name="provider")
public class DataProvider extends JPABaseEntity {

	@Id
	@SequenceGenerator(name="providerIdSeqGenerator", sequenceName="provider_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="providerIdSeqGenerator")
	protected Integer id;
	
	private String description;

	@Column(nullable=false, unique = true)
	private String name;
	
	public DataProvider() {
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

	@Override
	public Integer getVersion() {
		return null;
	}

	@Override
	public Integer getId() {
		return id;
	}

}