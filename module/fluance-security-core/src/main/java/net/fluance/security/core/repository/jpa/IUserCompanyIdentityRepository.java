/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import net.fluance.security.core.model.jpa.UserCompanyIdentity;
import net.fluance.security.core.model.jpa.UserCompanyIdentityPK;

@Repository
public interface IUserCompanyIdentityRepository extends JpaRepository<UserCompanyIdentity, UserCompanyIdentityPK> {
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO profile_identity(profile_id, company_id, provider_id, staffid) VALUES((SELECT id FROM profile WHERE username = ?1 AND domainname=?2), ?3, ?4, ?5)", nativeQuery = true)
	public int saveByUsernameAndDomainName(String username, String domainName, Integer companyId, Integer providerId, String staffId);	
	
	public List<UserCompanyIdentity> findByProfileId(Integer id);

	@Query(value = "SELECT * FROM profile_identity WHERE profile_id = (SELECT id FROM profile WHERE username = ?1 AND domainname=?2) ORDER BY company_id", nativeQuery = true)
	public List<UserCompanyIdentity> findByUsernameAndDomainName(String username, String domainName);
	
	public List<UserCompanyIdentity> findByCompanyId(Integer id);
	
	public List<UserCompanyIdentity> findByCompanyIdAndProviderId(Integer id, Integer providerId);
	
	public List<UserCompanyIdentity> findByProfileIdAndCompanyId(Integer profileId, Integer companyId);

	public List<UserCompanyIdentity> findByProfileIdAndCompanyIdAndProviderId(Integer profileId, Integer companyId, Integer providerId);
	
	@Query(value = "SELECT * FROM profile_identity WHERE profile_id = (SELECT id FROM profile WHERE username = ?1 AND domainname=?2) AND company_id = ?3 ORDER BY company_id", nativeQuery = true)
	public List<UserCompanyIdentity> findByUsernameAndDomainNameAndCompanyId(String username, String domainName, Integer companyId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompanyIdentity uc WHERE uc.companyId = ?1")
	void deleteByCompanyId(Integer companyId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompanyIdentity uc WHERE uc.profileId = ?1")
	void deleteByProfileId(Integer profileId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompanyIdentity uc WHERE uc.profileId = ?1 AND uc.companyId = ?2")
	void deleteByProfileIdAndCompanyId(Integer profileId, Integer companyId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_identity WHERE profile_id=(SELECT id FROM profile WHERE username = ?1 AND domainname=?2)", nativeQuery = true)
	void deleteByUsernameAndDomainName(String username, String domainName);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_identity WHERE profile_id =(SELECT id FROM profile WHERE username = ?1 AND domainname=?2) AND company_id = ?3", nativeQuery = true)
	void deleteByUsernameAndDomainNameAndCompanyId(String username, String domainName, Integer companyId);
}
