/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.fluance.app.spring.data.jpa.repository.IJPAGenericRepository;
import net.fluance.security.core.model.jpa.Role;

@Repository
public interface IRoleRepository extends IJPAGenericRepository<Role, Integer> {
	Role findByName(String name);
	
	@Query(value = "SELECT r.id, r.name, r.description FROM role r INNER JOIN profile_role pr ON pr.role_id = r.id WHERE pr.profile_id = ?1", nativeQuery = true)
	public List<Role> getByProfileId(Integer profileId);

}
