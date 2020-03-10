/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import net.fluance.app.spring.data.jpa.repository.IJPAGenericRepository;
import net.fluance.security.core.model.jpa.Action;

@NoRepositoryBean
public interface IActionRepository extends IJPAGenericRepository<Action, Long> {
	public List<Action> findByName(String name);
}
