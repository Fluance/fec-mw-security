package net.fluance.security.core.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;
import net.fluance.security.core.model.jdbc.UserInfo;

@Repository
@Component
public class UserInfoRepository extends JdbcRepository<UserInfo, String> {

	public UserInfoRepository() {
		super(ROW_MAPPER, ROW_UNMAPPER, "user_info");
	}

	public static final RowMapper<UserInfo> ROW_MAPPER = new RowMapper<UserInfo>() {
	    public UserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
	        return new UserInfo(
	                rs.getString("id"),
	                rs.getString("info")
	        );
	    }
	};

	private static final RowUnmapper<UserInfo> ROW_UNMAPPER = new RowUnmapper<UserInfo>() {
		public Map<String, Object> mapColumns(UserInfo userInfo) {
			Map<String, Object> mapping = new LinkedHashMap<String, Object>();
			mapping.put("id", userInfo.getId());
			mapping.put("info", userInfo.getUserInfo());
			return mapping;
		}
	};


	public void insertOrUpdate(UserInfo userInfo){
		if(this.findOne(userInfo.getId()) != null){
			this.save(userInfo);
		}
		else{
			getJdbcOperations().update("INSERT INTO user_info (id, info) VALUES (?, ?);"
					, userInfo.getId(), userInfo.getUserInfo());
		}
	}
}
