package net.fluance.security.core.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.fluance.security.core.model.partner.Partner;

public class TrustedPartnerUtils {
	
	/**
	 * Search the partners by name from the given list of {@link Partner}
	 * 
	 * @param partners
	 * @param partnerName
	 * @return
	 */
	public static Partner searchPartnerByName(List<Partner> partners, String partnerName) {
		Partner partner = null;
		
		if(StringUtils.isNotEmpty(partnerName) && partners!=null) {
			partner = partners.stream()
			  .filter(searchPartner -> partnerName.equals(searchPartner.getName()))
			  .findAny()
			  .orElse(null);
		}
		
		return partner;
	}
}
