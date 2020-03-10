package net.fluance.security.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;


/**
 * The persistent class for the client database table.
 * 
 */
@JsonIgnoreProperties(value={"version"}, ignoreUnknown = true)
@SuppressWarnings("serial")
@Table(name="action")
public class Action extends JPABaseEntity {

	@Id
	@SequenceGenerator(name="actionIdSeqGenerator", sequenceName="action_id_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="actionIdSeqGenerator")
	protected Long id;
	
	@Column(unique = true)
	private String name;
	
	private String description;

	public Action() {
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public Integer getVersion() {
		return null;
	}

}