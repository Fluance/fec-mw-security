/**
 * 
 */
package net.fluance.security.core.repository.jpa;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import net.fluance.security.core.model.jpa.Resource;

@NoRepositoryBean
public interface IResourceRepository extends JpaRepository<Resource, Long> {
	public List<Resource> findByKey(String key);
}
