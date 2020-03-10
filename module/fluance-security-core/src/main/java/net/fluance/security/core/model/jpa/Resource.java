package net.fluance.security.core.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The persistent class for the client database table.
 * 
 */
@JsonIgnoreProperties(value={"version"}, ignoreUnknown = true)
@SuppressWarnings("serial")
@Table(name="resource")
public class Resource implements Serializable {

	@Id
	@SequenceGenerator(name="resourceIdSeqGenerator", sequenceName="resource_id_seq")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="resourceIdSeqGenerator")
	protected Long id;
	
	@Column(unique=true)
	private String key;
	
	@SuppressWarnings("unused")
	private String name;

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
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

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

}