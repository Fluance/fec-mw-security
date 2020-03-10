/**
 * 
 */
package net.fluance.security.core.support.helper;

import java.util.UUID;

public class ClientHelper {

	/**
	 * 
	 * @return
	 */
	public final UUID generateClientId() {
		return UUID.randomUUID();
	}
	
}
