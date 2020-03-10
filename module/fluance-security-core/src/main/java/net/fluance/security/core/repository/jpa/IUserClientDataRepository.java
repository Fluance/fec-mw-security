/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.fluance.security.core.model.jpa.UserClientData;
import net.fluance.security.core.model.jpa.UserClientDataPK;

@Repository
public interface IUserClientDataRepository extends JpaRepository<UserClientData, UserClientDataPK> {

	List<UserClientData> findByClientId(UUID clientId);
	
	List<UserClientData> findByProfileId(Integer profileId);
	
	UserClientData findByProfileIdAndClientId(Integer profileId, UUID clientId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserClientData ucd WHERE ucd.clientId = ?1")
	void deleteByClientId(UUID clientId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserClientData ucd WHERE ucd.profileId = ?1")
	void deleteByProfileId(Integer userId);
}
