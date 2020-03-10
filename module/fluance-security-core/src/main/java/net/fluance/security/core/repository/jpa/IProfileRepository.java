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

import net.fluance.security.core.model.jpa.Profile;

@Repository
public interface IProfileRepository extends JpaRepository<Profile, Integer> {

	/**
	 * 
	 * @param username
	 * @param domainName
	 * @param usertype (NAME, NOT ID!)
	 * @return
	 */
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO profile (username, domainname, usertype_id) VALUES(?1, ?2, (SELECT id FROM usertype WHERE type=?3))", nativeQuery = true)
	public int saveByUsernameDomainNameAndUsertype(String username, String domainName, String usertype);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO profile_role (profile_id, role_id) VALUES(?1, (SELECT id FROM role WHERE name = ?2))", nativeQuery = true)
	public int insertUserRoleByProfileId(Integer profileId, String role);
	
	public List<Profile> findByUsername(String username);
	
	@Query(value = "SELECT * FROM profile WHERE username = ?1 AND domainname=?2", nativeQuery = true)
	public Profile findByUsernameAndDomainName(String userName, String domainName);

	@Query(value = "SELECT * FROM profile WHERE username = ?1 AND domainname=?2", nativeQuery = true)
	public List<Profile> findProfilesByUsernameAndDomainName(String userName, String domainName);

	@Query(value = "SELECT * FROM profile WHERE username ilike(concat(?1 , '%')) AND domainname=?2", nativeQuery = true)
	public List<Profile> findByUsernameBeginningWith(String username, String domainName);	
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM profile WHERE username = ?1 AND domainname=?2", nativeQuery = true)
	public void deleteByUsernameAndDomainName(String username, String domainName);

	@Query(value = "SELECT * FROM profile WHERE id = (SELECT profile_id FROM profile_identity WHERE staffid=?1 AND company_id=?2 AND provider_id=?3)", nativeQuery = true)
	public List<Profile> findByStaffIds(String staffId, Long cliniqueId, Long providerId);
}
