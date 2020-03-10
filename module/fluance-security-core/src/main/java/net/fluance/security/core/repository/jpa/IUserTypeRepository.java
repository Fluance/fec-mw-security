package net.fluance.security.core.repository.jpa;

import org.springframework.stereotype.Repository;

import net.fluance.app.spring.data.jpa.repository.IJPAGenericRepository;
import net.fluance.security.core.model.jpa.UserType;

@Repository
public interface IUserTypeRepository extends IJPAGenericRepository<UserType, Integer> {

	UserType findByName(String name);
}