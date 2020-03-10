/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import org.springframework.stereotype.Repository;

import net.fluance.app.spring.data.jpa.repository.IJPAGenericRepository;
import net.fluance.security.core.model.jpa.DataProvider;

@Repository
public interface IDataProviderRepository extends IJPAGenericRepository<DataProvider, Integer> {
	
	DataProvider findByName(String name);
	
}
