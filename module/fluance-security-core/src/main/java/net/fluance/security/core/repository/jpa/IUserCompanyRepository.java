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

import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.model.jpa.UserCompanyPK;

@Repository
public interface IUserCompanyRepository extends JpaRepository<UserCompany, UserCompanyPK> {
	
	public List<UserCompany> findByProfileId(Integer id);

	public List<UserCompany> findByProfileIdAndCompanyId(Integer profileId, Integer companyId);

	@Query(value = "SELECT * FROM profile_permission WHERE profile_id in (SELECT id FROM profile WHERE username = ?1 AND domainname = ?2) ORDER BY company_id", nativeQuery = true)
	public List<UserCompany> findByUsernameAndDomainName(String userName, String domainName);

	@Query(value = "SELECT * FROM profile_permission WHERE profile_id in (SELECT id FROM profile WHERE username = ?1 AND domainname = ?2) AND company_id = ?3 ORDER BY company_id", nativeQuery = true)
	public UserCompany findByUsernameAndDomainNameAndCompanyId(String username, String domainName, Integer companyId);
	
	public List<UserCompany> findByCompanyId(Integer id);
	
	@Query(value = "SELECT * FROM profile_permission WHERE company_id=(SELECT id FROM company WHERE code=?1)", nativeQuery = true)
	public List<UserCompany> findByCompanyCode(String code);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_permission WHERE company_id=(SELECT id FROM company WHERE code=?1)", nativeQuery = true)
	void deleteByCompanyCode(String code);
	
	@Modifying
	@Transactional
	@Query("UPDATE UserCompany uc SET uc.unitsAndServices = to_json(?3) WHERE uc.companyId = ?1 AND uc.companyId = ?2")
	int updateUnitsAndServicesByProfileIdAndCompanyId(Integer profileId, Integer companyId, String unitsAndServices);

	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompany uc WHERE uc.companyId = ?1")
	void deleteByCompanyId(Integer companyId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompany uc WHERE uc.profileId = ?1")
	void deleteByProfileId(Integer profileId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM UserCompany uc WHERE uc.profileId = ?1 AND uc.companyId = ?2")
	void deleteByProfileIdAndCompanyId(Integer profileId, Integer companyId);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_permission WHERE profile_id=(SELECT id FROM profile WHERE username = ?1 AND domainname=?2)", nativeQuery = true)
	void deleteByUsernameAndDomainName(String username, String domainName);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile_permission WHERE profile_id =(SELECT id FROM profile WHERE username = ?1 AND domainname=?2) AND company_id = ?3", nativeQuery = true)
	void deleteByUsernameAndDomainNameAndCompanyId(String username, String domainName, Integer companyId);
}
