/**
 * 
 */
package net.fluance.security.core.repository.jpa;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.fluance.app.spring.data.jpa.repository.IJPAGenericRepository;
import net.fluance.security.core.model.jpa.Client;

@Repository
public interface IClientRepository extends IJPAGenericRepository<Client, UUID> {
	public Client findByIdAndSecret(UUID id, String secret);
	public Client findByName(String name);
}
