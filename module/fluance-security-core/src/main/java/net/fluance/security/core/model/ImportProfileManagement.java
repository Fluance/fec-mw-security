package net.fluance.security.core.model;

import java.util.ArrayList;
import java.util.List;

import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.security.core.model.response.ImportFailureResponse;

/**
 * Contain the  Profiles which are possible to save and the discarted with the why reason
 *
 */
public class ImportProfileManagement {
	
	private List<EhProfile> success = new ArrayList<>();
	private List<ImportFailureResponse> failure = new ArrayList<>();
	
	public List<EhProfile> getSuccess() {
		return success;
	}
	
	public void setSuccess(List<EhProfile> success) {
		this.success = success;
	}
	
	public List<ImportFailureResponse> getFailure() {
		return failure;
	}
	
	public void setFailure(List<ImportFailureResponse> failure) {
		this.failure = failure;
	}
	
}