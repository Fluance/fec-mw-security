package net.fluance.security.ehprofile.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.fluance.app.data.model.identity.AccessControl;
import net.fluance.app.data.model.identity.CompanyStaffId;
import net.fluance.app.data.model.identity.EhProfile;
import net.fluance.app.data.model.identity.GrantedCompany;
import net.fluance.app.data.model.identity.HospService;
import net.fluance.app.data.model.identity.PatientUnit;
import net.fluance.app.data.model.identity.UserType;
import net.fluance.app.web.util.exceptions.BadRequestException;
import net.fluance.security.core.model.ImportProfileManagement;
import net.fluance.security.core.model.jpa.UserCompany;
import net.fluance.security.core.model.response.ImportFailureResponse;
import net.fluance.security.core.model.response.ImportProfileResult;
import net.fluance.security.core.model.response.ImportSuccessResponse;
import net.fluance.security.core.repository.jpa.IRoleRepository;
import net.fluance.security.core.repository.jpa.IUserCompanyRepository;
import net.fluance.security.ehprofile.util.EhProfileUtils;

@Service
public class AdminService {

	private static final Logger LOGGER = LogManager.getLogger(AdminService.class);
	
	@Autowired
	private UserProfileService userProfileService;
	
	@Autowired
	private IUserCompanyRepository userCompanyRepository;
	
	@Autowired
	IRoleRepository roleRepository;

	private static final Integer HEADER_ROW = 0;
	
	@Value("${security.admin.profile.import.username}")
	private Integer USERNAME_COLUMN;
	@Value("${security.admin.profile.import.opale}")
	private Integer OPALE_COLUMN;
	@Value("${security.admin.profile.import.polypoint}")
	private Integer POLYPOINT_COLUMN;
	@Value("${security.admin.profile.import.role}")
	private Integer ROLE_COLUMN;
	@Value("${security.admin.profile.import.company}")
	private Integer COMPANY_COLUMN;
	@Value("${security.admin.profile.import.hospservice}")
	private Integer HOSPSERVICE_COLUMN;
	@Value("${security.admin.profile.import.units}")
	private Integer UNITS_COLUMN;
	
	private static final String DOMAIN_PRIMARY = "PRIMARY";
	private static final String DEFAULT_LANGUAGE = "en";
	
	private static final Integer OPALE_PROVIDER = 1;
	private static final Integer POLYPOINT_PROVIDER = 2;
	
	/**
	 * Read a file to extract the Profiles and checks which are possible to save and which ones discard
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public ImportProfileManagement load(MultipartFile file) throws Exception {
		
		ImportProfileManagement results = new ImportProfileManagement(); 
		
		Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file.getBytes()));
		Sheet sheet = workbook.getSheetAt(0);
		
		List<String> rejectedUsers = new ArrayList<>();
		List<String> usedUsers = new ArrayList<>();
		
		for(Row row : sheet){
			if(row.getRowNum() != HEADER_ROW) {
				
				String username = getColumnAsStringValue(row, USERNAME_COLUMN);
				if(username != null && !username.isEmpty()) {
					
					Boolean existProfile = new Boolean(false);
					
					if(!rejectedUsers.contains(username) && !usedUsers.contains(username)){
						existProfile = userProfileService.exists(username, DOMAIN_PRIMARY);
					}
					
					if(existProfile && !rejectedUsers.contains(username)) {
						rejectedUsers.add(username);
						moveFromSuccessToFailure(results, username, "The User has already Profiles in the system");
					}
					else if (!existProfile && !rejectedUsers.contains(username)) {
						try {
							if(usedUsers.contains(username)) {
								EhProfile editingProfile = EhProfileUtils.getProfileByUsername(results.getSuccess(), username);
								
								if(editingProfile == null){
									rejectedUsers.add(username);
									moveFromSuccessToFailure(results, username, "Profile Not Updatable");
								}
								
								String role = getColumnAsStringValue(row, ROLE_COLUMN);
								
								if(!StringUtils.isEmpty(role)) {
									if(!editingProfile.getGrants().getRoles().contains(role)) {
										editingProfile.getGrants().getRoles().add(role);
									}
								}
								
								String companyCode = getColumnAsStringValue(row, COMPANY_COLUMN);
								
								if(!StringUtils.isEmpty(companyCode)) {
									GrantedCompany editingGrantedCompany = EhProfileUtils.getGrantedCompany(editingProfile, companyCode);
									if(editingGrantedCompany == null) {
										GrantedCompany newGrantedCompany = addNewGrantedCompany(row);
										editingProfile.getGrants().addGrantedCompany(newGrantedCompany);
									} else {
										editGrantedCompany(row, editingGrantedCompany);
									}
								} else {
									rejectedUsers.add(username);
									moveFromSuccessToFailure(results, username, "The Profile cannot be imported without Company");
								}
							}
							else {
								EhProfile newProfile = createEhProfile(row, username);
								usedUsers.add(username);
								results.getSuccess().add(newProfile);
							}
						} catch(BadRequestException bre) {
							rejectedUsers.add(username);
							moveFromSuccessToFailure(results, username, bre.getMessage());
						}
					}
				}
			}
		}
		workbook.close();
		return results;
	}
	
	public List<String> findAllRoles() {
		List<String> roleNames = new ArrayList<>();
		
		roleRepository.findAll().stream().forEach(role -> roleNames.add(role.getName()));
		
		return roleNames;
	}
	
	/**
	 * From a {@link Row}, creates a {@link GrantedCompany} with the Non-Empty info
	 * @param row
	 * @return
	 */
	private GrantedCompany addNewGrantedCompany(Row row) {
		String codeCompany = getColumnAsStringValue(row, COMPANY_COLUMN);
		
		UserCompany userCompany = new UserCompany();
		if(!StringUtils.isEmpty(codeCompany)) {
			List<UserCompany> companies = userCompanyRepository.findByCompanyCode(codeCompany);
			if(companies != null && !companies.isEmpty()){
				userCompany = companies.get(0);
			} else {
				throw new BadRequestException("The Profiles must have a valid/existing Company");
			}
			
			List<PatientUnit> patientunits = new ArrayList<>();
			String unit = getColumnAsStringValue(row, UNITS_COLUMN);
			if(!StringUtils.isEmpty(unit)) {
				PatientUnit patientUnit = new PatientUnit(unit);
				patientunits.add(patientUnit);
			}
			
			List<HospService> hospservices = new ArrayList<>();
			String codeHospService = getColumnAsStringValue(row, HOSPSERVICE_COLUMN);
			if(!StringUtils.isEmpty(codeHospService)){
				HospService hospService = new HospService(codeHospService);
				hospservices.add(hospService);
			}
			
			List<CompanyStaffId> staffIds =  new ArrayList<>();			
			String opaleId = getColumnAsStringValue(row, OPALE_COLUMN);		
			if(!StringUtils.isEmpty(opaleId)){
				CompanyStaffId staffId = new CompanyStaffId();
				staffId.setProviderId(OPALE_PROVIDER);
				staffId.setStaffId(opaleId);
				staffIds.add(staffId);
			} 
			
			String polypointId = getColumnAsStringValue(row, POLYPOINT_COLUMN);
			if(!StringUtils.isEmpty(polypointId)){
				CompanyStaffId staffId = new CompanyStaffId();
				staffId.setProviderId(POLYPOINT_PROVIDER);
				staffId.setStaffId(polypointId);
				staffIds.add(staffId);
			}
			
			return new GrantedCompany(userCompany.getCompanyId(), codeCompany, patientunits, hospservices, staffIds);
		}
		else {
			throw new BadRequestException("The Profile cannot be imported without Company");
		}
	}

	/**
	 * From a {@link Row}, creates a {@link EhProfile} with the Non-Empty info
	 * @param row
	 * @param username
	 * @return
	 */
	private EhProfile createEhProfile(Row row, String username) {
		
		List<String> roles = new ArrayList<>();
		String role = getColumnAsStringValue(row, ROLE_COLUMN);
		if(!StringUtils.isEmpty(role)) {
			roles.add(role);
		}
		
		String codeCompany = getColumnAsStringValue(row, COMPANY_COLUMN);
		UserCompany userCompany = new UserCompany();
		if(!StringUtils.isEmpty(codeCompany)) {								
			List<UserCompany> companies = userCompanyRepository.findByCompanyCode(codeCompany);
			if(companies != null && !companies.isEmpty()){
				userCompany = companies.get(0);
			}
			else {
				throw new BadRequestException("The Profiles must have a valid/existing Company");
			}
		}
		else {
			throw new BadRequestException("The Profile cannot be imported without Company");
		}
		
		List<PatientUnit> patientunits = new ArrayList<>();
		String unit = getColumnAsStringValue(row, UNITS_COLUMN);
		if(!StringUtils.isEmpty(unit)) {
			PatientUnit patientUnit = new PatientUnit(unit);
			patientunits.add(patientUnit);
		}
		
		List<HospService> hospservices = new ArrayList<>();
		String codeHospService = getColumnAsStringValue(row, HOSPSERVICE_COLUMN);
		if(!StringUtils.isEmpty(codeHospService)){
			HospService hospService = new HospService(codeHospService);
			hospservices.add(hospService);
		}
		
		List<CompanyStaffId> staffIds =  new ArrayList<>();
		
		String opaleId = getColumnAsStringValue(row, OPALE_COLUMN);		
		if(!StringUtils.isEmpty(opaleId)){
			CompanyStaffId staffId = new CompanyStaffId();
			staffId.setProviderId(OPALE_PROVIDER);
			staffId.setStaffId(opaleId);
			staffIds.add(staffId);
		} 
		
		String polypointId = getColumnAsStringValue(row, POLYPOINT_COLUMN);
		if(!StringUtils.isEmpty(polypointId)){
			CompanyStaffId staffId = new CompanyStaffId();
			staffId.setProviderId(POLYPOINT_PROVIDER);
			staffId.setStaffId(polypointId);
			staffIds.add(staffId);
		}
		
		GrantedCompany grantedCompany = new GrantedCompany(userCompany.getCompanyId(), codeCompany, patientunits , hospservices, staffIds);
		List<GrantedCompany> grantedCompanies = new ArrayList<>();
		grantedCompanies.add(grantedCompany);
		AccessControl grants = new AccessControl(grantedCompanies, roles);
		
		return new EhProfile(null, DOMAIN_PRIMARY, username, DEFAULT_LANGUAGE, UserType.USER, grants);
	}

	/**
	 * From a {@link Row} and a {@link GrantedCompany}, updates its content with the Non-Empty info
	 * @param row
	 * @param grantedCompany
	 */
	private void editGrantedCompany(Row row, GrantedCompany grantedCompany){
		
		String unitCode = getColumnAsStringValue(row, UNITS_COLUMN);
		if(!StringUtils.isEmpty(unitCode) && !isUnitDuplicated(unitCode, grantedCompany.getPatientunits())) {
			PatientUnit patientUnit = new PatientUnit(unitCode);
			grantedCompany.getPatientunits().add(patientUnit);
		}
		
		String hospServiceCode = getColumnAsStringValue(row, HOSPSERVICE_COLUMN);
		if(!StringUtils.isEmpty(hospServiceCode) && !isHospServiceDuplicated(hospServiceCode, grantedCompany.getHospservices())) {
			HospService e = new HospService(hospServiceCode);
			grantedCompany.getHospservices().add(e );
		}
		
		CompanyStaffId existingOpale = EhProfileUtils.getCompanyStaffId(grantedCompany, OPALE_PROVIDER);
		String opaleStaffId =  getColumnAsStringValue(row, OPALE_COLUMN);
		// FIXME: Only the first StaffId loaded is saved
		if(existingOpale==null && !StringUtils.isEmpty(opaleStaffId)){
			CompanyStaffId companyStaffId = new CompanyStaffId();
			companyStaffId.setProviderId(OPALE_PROVIDER);
			companyStaffId.setStaffId(opaleStaffId);
			grantedCompany.getStaffIds().add(companyStaffId);
		}
		
		CompanyStaffId existingPolipoint = EhProfileUtils.getCompanyStaffId(grantedCompany, POLYPOINT_PROVIDER);
		String polipointStaffId =  getColumnAsStringValue(row, POLYPOINT_COLUMN);
		// FIXME: Only the first StaffId loaded is saved
		if(existingPolipoint==null && !StringUtils.isEmpty(polipointStaffId)){
			CompanyStaffId companyStaffId = new CompanyStaffId();
			companyStaffId.setProviderId(POLYPOINT_PROVIDER);
			companyStaffId.setStaffId(polipointStaffId);
			grantedCompany.getStaffIds().add(companyStaffId);
		}
	}
	
	/**
	 * Checks if the Unit Code is already in the list
	 * @param unitCode
	 * @param patientunits
	 * @return
	 */
	private Boolean isUnitDuplicated(String unitCode, List<PatientUnit> patientunits) {
		if(unitCode==null) {
			return false;
		}
		PatientUnit existsUnit = patientunits.stream().filter(unit -> unitCode.equals(unit.getCode())).findAny().orElse(null);
		if(existsUnit != null){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if the HospService Code is already in the list
	 * @param hospServCode
	 * @param hospServices
	 * @return
	 */
	private Boolean isHospServiceDuplicated(String hospServCode, List<HospService> hospServices){
		if(hospServCode==null) {
			return false;
		}
		HospService existsHospService = hospServices.stream().filter(hospService -> hospServCode.equals(hospService.getCode())).findAny().orElse(null);
		if(existsHospService != null){
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Given a {@link Row} and a Column Number, returns its value as a {@link String} for any Cell Type
	 * @param row
	 * @param column
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String getColumnAsStringValue(Row row, Integer column) {
		Cell cell = row.getCell(column);
		if(cell == null){
			return null;
		}
		switch (cell.getCellTypeEnum()) {
			case STRING: 
            	return cell.getStringCellValue();
            case NUMERIC: 
            	Double value = row.getCell(column).getNumericCellValue();
            	return value!= null ? String.valueOf(value.intValue()) : null;
            default:
            	return null;
		}
	}
	
	/**
	 * Given a {@link ImportProfileManagement}, it gets the success  {@link EhProfile}, if exists, and moves it to the failure list with a message
	 * @param results
	 * @param username
	 * @param message
	 */
	private void moveFromSuccessToFailure(ImportProfileManagement results, String username, String message) {
		EhProfile profileMoving = results.getSuccess().stream().filter(profile -> username.equals(profile.getUsername())).findFirst().orElse(null);
		if(profileMoving != null) {
			results.getSuccess().remove(profileMoving);
		}
		ImportFailureResponse failureResponse = new ImportFailureResponse(username, message);
		results.getFailure().add(failureResponse);
	}
	
	
	/**
	 * Imports the given file that must be an Excel with at least one profile
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public ImportProfileResult importProfileFile(MultipartFile file) throws Exception {
		ImportProfileManagement profilesLoaded = load(file);
		
		ImportProfileResult importResult = new ImportProfileResult();
		importResult.setFailure(profilesLoaded.getFailure());
		
		for(EhProfile profile : profilesLoaded.getSuccess()){
			try{
				userProfileService.create(profile);
				importResult.getSuccess().add(new ImportSuccessResponse(profile.getUsername()));
			} catch (DataAccessException dae){
				ImportFailureResponse exp = new ImportFailureResponse(profile.getUsername(), "The profile data does not comply with the integrity of the information in the Database");
				importResult.getFailure().add(exp);
			} catch(Exception e){
				ImportFailureResponse exp = new ImportFailureResponse(profile.getUsername(), e.getMessage());
				importResult.getFailure().add(exp);
			}
		}
		return importResult;
	}
	
	public static Logger getLogger() {
		return LOGGER;
	}
}