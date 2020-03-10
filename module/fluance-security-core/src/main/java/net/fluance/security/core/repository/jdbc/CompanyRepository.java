/**
 * 
 */
package net.fluance.security.core.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;

import net.fluance.commons.sql.SqlUtils;
import net.fluance.security.core.model.jdbc.Company;

@Repository
public class CompanyRepository extends JdbcRepository<Company, Integer> {

	public CompanyRepository() {
		super(ROW_MAPPER, ROW_UNMAPPER, "company");
	}
	
	public static final RowMapper<Company> ROW_MAPPER = new RowMapper<Company>() {
	    public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
	        return new Company(
	                SqlUtils.getInt(true, rs, "id"),
	                rs.getString("code")
	        );
	    }
	};
	
	private static final RowUnmapper<Company> ROW_UNMAPPER = new RowUnmapper<Company>() {
	    public Map<String, Object> mapColumns(Company company) {
	        Map<String, Object> mapping = new LinkedHashMap<String, Object>();
	        mapping.put("id", company.getId());
	        mapping.put("code", company.getCode());
	        return mapping;
	    }
	};
	
}