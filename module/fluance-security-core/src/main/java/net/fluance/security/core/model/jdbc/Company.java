package net.fluance.security.core.model.jdbc;

import org.springframework.data.domain.Persistable;


/**
 * The persistent class for the company database table.
 * 
 */
@SuppressWarnings("serial")
public class Company implements Persistable<Integer> {

	private Integer id;
	private String code;
	
	public Company() {
	}

	/**
	 * @param id
	 * @param code
	 */
	public Company(Integer id, String code) {
		super();
		this.id = id;
		this.code = code;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}


	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public boolean isNew() {
		// id must be null for a new (unsaved) entity
		// when the id is auto-generated
		return id == null;
	}
	
}