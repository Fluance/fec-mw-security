package net.fluance.security.core.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Response with the profiles tryed to import, splitted in successful and failures
 *
 */
public class ImportProfileResult {
	
	private List<ImportSuccessResponse> success;
	private List<ImportFailureResponse> failure;
		
	public ImportProfileResult() {
		super();
		this.success = new ArrayList<>();
		this.failure = new ArrayList<>();
	}
	
	public List<ImportSuccessResponse> getSuccess() {
		return success;
	}
	
	public void setSuccess(List<ImportSuccessResponse> success) {
		this.success = success;
	}
	
	public List<ImportFailureResponse> getFailure() {
		return failure;
	}
	
	public void setFailure(List<ImportFailureResponse> failure) {
		this.failure = failure;
	} 
}
