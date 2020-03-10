/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import net.fluance.security.core.model.jpa.Permission;
import net.fluance.security.core.model.jpa.PermissionPK;

@NoRepositoryBean
public interface IPermissionRepository extends JpaRepository<Permission, PermissionPK> {
	
	public List<Permission> findByRoleId(Integer role);
	
	public List<Permission> findByActionId(Long action);
	
	public List<Permission> findByResourceId(Long resource);
	
	public List<Permission> findByRoleIdAndResourceId(Long role, Long resource);
	
	public List<Permission> findByActionIdAndRoleId(Long action, Long resource);
	
	public List<Permission> findByActionIdAndResourceId(Long action, Long resource);
	
	public List<Permission> findByActionIdAndResourceIdAndRoleId(Long action, Long resource, Long role);
	
	public List<Permission> findByActionIdAndResourceIdAndRoleIdIn(Long action, Long resource, List<Long> roles);
	
	@Query("SELECT p FROM Permission p "
			+ "WHERE p.actionId = (SELECT a.id FROM Action a WHERE name = ?1) "
			+ "AND p.resourceId = (SELECT res.id FROM Resource res WHERE key = ?2) "
			+ "AND p.roleId = (SELECT r.id FROM Role r WHERE name = ?3)")
	public List<Permission> findByActionNameAndResourceKeyAndRoleName(String action, String resourceKey, String role);
	
	@Query("SELECT p FROM Permission p "
			+ "WHERE p.actionId = (SELECT a.id FROM Action a WHERE name = ?1) "
			+ "AND p.resourceId = (SELECT res.id FROM Resource res WHERE key = ?2) "
			+ "AND p.roleId IN (SELECT r.id FROM Role r WHERE name IN ?3)")
	public List<Permission> findByActionNameAndResourceAndRoleNameIn(String action, String resourceKey, List<String> roles);
	
	@Query(value = "SELECT * FROM role_permission "
			+ "WHERE action_id = (SELECT id FROM action WHERE name = ?1) "
			+ "AND resource_id = (SELECT id FROM resource WHERE ?2 ~* key) "
			+ "AND role_id = (SELECT id FROM role WHERE name = ?3)", nativeQuery = true)
	public List<Permission> findByActionNameAndResourceMatchingAndRoleName(String action, String resource, String role);
	
	@Query(value = "SELECT * FROM role_permission "
			+ "WHERE action_id = (SELECT id FROM action WHERE name = ?1) "
			+ "AND resource_id = (SELECT id FROM resource WHERE ?2 ~* key) "
			+ "AND role_id IN ((SELECT id FROM role WHERE name IN (?3)))", nativeQuery = true)
	public List<Permission> findByActionNameAndResourceMatchingAndRoleNameIn(String action, String resource, List<String> roles);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.actionId = (SELECT a.id FROM Action a WHERE a.name = ?1)")
	public void deleteByActionName(String actionName);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.resourceId = (SELECT res.id FROM Resource res WHERE res.key = ?1)")
	public void deleteByResourceKey(String resourceKey);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.roleId = (SELECT r.id FROM Role r WHERE r.name = ?1)")
	public void deleteByRoleName(String roleName);
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.roleId = ?1")
	public void deleteByRoleId(Integer roleId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.actionId = ?1")
	public void deleteByActionId(Integer actionId);
	
	@Modifying
	@Transactional
	@Query("DELETE FROM Permission p WHERE p.resourceId = ?1")
	public void deleteByResourceId(Integer resourceId);
	
}
