package net.fluance.security.core.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;

import net.fluance.commons.sql.SqlUtils;
import net.fluance.security.core.model.jdbc.Idp;

@Repository
@Component
public class IdpRepository extends JdbcRepository<Idp, Integer> {

	public IdpRepository() {
		super(ROW_MAPPER, ROW_UNMAPPER, "idp");
	}

	public static final RowMapper<Idp> ROW_MAPPER = new RowMapper<Idp>() {
	    public Idp mapRow(ResultSet rs, int rowNum) throws SQLException {
	        return new Idp(
	        		SqlUtils.getInt(true, rs, "id"),
	                rs.getString("display_name"),
	                rs.getString("url"),
	                rs.getString("img_url")
	        );
	    }
	};

	private static final RowUnmapper<Idp> ROW_UNMAPPER = new RowUnmapper<Idp>() {
		public Map<String, Object> mapColumns(Idp idps) {
			Map<String, Object> mapping = new LinkedHashMap<String, Object>();
			mapping.put("id", idps.getId());
			mapping.put("display_name", idps.getDisplayName());
			mapping.put("url", idps.getUrl());
			mapping.put("img_url", idps.getImgUrl());
			return mapping;
		}
	};

	@Override
	@Cacheable("findAllIdps")
	public List<Idp> findAll() {
//		this.setJdbcOperations(getJdbcOperations());
		return super.findAll();
	}
}
