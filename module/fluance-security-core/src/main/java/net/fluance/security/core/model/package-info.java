
@TypeDefs({ 
	@TypeDef(name = "JpaJsonObject", typeClass = JsonObjectType.class) 
})

package net.fluance.security.core.model;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import net.fluance.app.spring.data.support.orm.hibernate.type.JsonObjectType;
