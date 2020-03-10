/**
 * 
 */
package net.fluance.security.core.repository.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.fluance.security.core.model.jpa.Profile;
import net.fluance.security.core.model.jpa.ProfileMetadata;

@Repository
public interface IProfileMetadataRepository extends JpaRepository<ProfileMetadata, Integer> {}
