package net.fluance.security.core.repository.jdbc;

import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.nurkiewicz.jdbcrepository.JdbcRepository;
import com.nurkiewicz.jdbcrepository.RowUnmapper;

import net.fluance.security.core.model.jdbc.UserSessionData;

@Repository
@Component
public class UserSessionDataRepository extends JdbcRepository<UserSessionData, String> {

	public UserSessionDataRepository() {
		super(ROW_MAPPER, ROW_UNMAPPER, "user_session_data");
	}

	public static final RowMapper<UserSessionData> ROW_MAPPER = new RowMapper<UserSessionData>() {
	    public UserSessionData mapRow(ResultSet rs, int rowNum) throws SQLException {
	        return new UserSessionData(
	                rs.getString("id"),
	                rs.getString("issuer"),
	                rs.getString("subject_id"),
	                rs.getString("session_index"),
	                rs.getString("agent"),
	                rs.getString("ipaddress"),
	                rs.getTimestamp("creationdt"),
	                rs.getTimestamp("expirationdt")
	        );
	    }
	};

	private static final RowUnmapper<UserSessionData> ROW_UNMAPPER = new RowUnmapper<UserSessionData>() {
		public Map<String, Object> mapColumns(UserSessionData userSessionData) {
			Map<String, Object> mapping = new LinkedHashMap<String, Object>();
			mapping.put("id", userSessionData.getId());
			mapping.put("issuer", userSessionData.getIssuer());
			mapping.put("subject_id", userSessionData.getSubjectId());
			mapping.put("session_index", userSessionData.getSessionIndex());
			mapping.put("agent", userSessionData.getAgent());
			mapping.put("ipaddress", userSessionData.getIpAddress());
			mapping.put("creationdt", userSessionData.getCreationDate());
			mapping.put("expirationdt", userSessionData.getExpirationDate());
			return mapping;
		}
	};

	public void insertUserAgent(String id, String agent, String ipaddress){
		this.setJdbcOperations(getJdbcOperations());
		if(this.findOne(id) != null){
			getJdbcOperations().update("UPDATE user_session_data SET agent = ?, ipaddress = ? WHERE id = ?"
					,agent, ipaddress, id);
		}
	}
	
	public void insertOrUpdate(UserSessionData userSessionData){
		this.setJdbcOperations(getJdbcOperations());
		if(this.findOne(userSessionData.getId()) != null){
			this.save(userSessionData);
		}
		else{
			getJdbcOperations().update("INSERT INTO user_session_data (id, issuer, subject_id, session_index, agent, ipaddress, creationdt, expirationdt) VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
					, userSessionData.getId(), userSessionData.getIssuer(), userSessionData.getSubjectId(), userSessionData.getSessionIndex(), userSessionData.getAgent(),
					userSessionData.getIpAddress(), userSessionData.getCreationDate(), userSessionData.getExpirationDate());
		}
	}

	/**
	 * Returns a {@link List} of the {@link UserSessionData} with the Subject ID (domain\\user) given as parameter
	 *  
	 * @param subjectId
	 * @return
	 */
	public List<UserSessionData> findTokenFromUser(String subjectId) {
		if(subjectId == null || subjectId.isEmpty()) {
			throw new InvalidParameterException("User Session Data cannot be null");
		}
		
		String sql = "SELECT * from user_session_data usd where usd.subject_id = ?";
		
		List<UserSessionData> lUserSessionData = getJdbcOperations().query(sql, ROW_MAPPER, subjectId);
		
		return lUserSessionData;
	}
	
}
