package net.fluance.security.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.fluance.app.spring.data.jpa.model.JPABaseEntity;

/**
 * The persistent class for the company database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "profile_metadata")
public class ProfileMetadata extends JPABaseEntity {

	@Id
	@Column(name = "profile_id")
	protected Integer profileId;
	@Column(name = "gender")
	private String gender;
	@Column(name = "birthdate")
	private String birthDate;
	@Column(name = "title")
	private String title;
	@Column(name = "speciality")
	private String speciality;
	@Column(name = "googletoken")
	private String googleToken;
	@Column(name = "linkedintoken")
	private String linkedInToken;
	@Column(name = "email")
	private String email;
	@Column(name = "externalphonenumberone")
	private String externalPhoneNumberOne;
	@Column(name = "externalphonenumbertwo")
	private String externalPhoneNumbertwo;
	@Column(name = "latitude")
	private String latitude;
	@Column(name = "longitude")
	private String longitude;
	@Column(name = "pictureuri")
	private String pictureUri;
	@Column(name = "employeeclinicid")
	private Integer employeeClinicId;
	@Column(name = "lastlocalizationat")
	private String lastLocalizationAt;
	@Column(name = "preferredphonenumber")
	private String preferredPhoneNumber;
	@Column(name = "supportcontactname")
	private String supportContactName;
	@Column(name = "supportcontactphonenumber")
	private String supportContactPhoneNumber;
	@Column(name = "lastactivityat")
	private String lastActivityAt; 
	@Column(name = "iban")
	private String iban; 

	public ProfileMetadata() {}

	public Integer getProfileId() {
		return profileId;
	}

	public void setProfileId(Integer profileId) {
		this.profileId = profileId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSpeciality() {
		return speciality;
	}

	public void setSpeciality(String speciality) {
		this.speciality = speciality;
	}

	public String getGoogleToken() {
		return googleToken;
	}

	public void setGoogleToken(String googleToken) {
		this.googleToken = googleToken;
	}

	public String getLinkedInToken() {
		return linkedInToken;
	}

	public void setLinkedInToken(String linkedInToken) {
		this.linkedInToken = linkedInToken;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getExternalPhoneNumberOne() {
		return externalPhoneNumberOne;
	}

	public void setExternalPhoneNumberOne(String externalPhoneNumberOne) {
		this.externalPhoneNumberOne = externalPhoneNumberOne;
	}

	public String getExternalPhoneNumbertwo() {
		return externalPhoneNumbertwo;
	}

	public void setExternalPhoneNumbertwo(String externalPhoneNumbertwo) {
		this.externalPhoneNumbertwo = externalPhoneNumbertwo;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getPictureUri() {
		return pictureUri;
	}

	public void setPictureUri(String pictureUri) {
		this.pictureUri = pictureUri;
	}

	@JsonIgnore
	@Override
	public Object getId() {
		return this.profileId;
	}

	@JsonIgnore
	@Override
	public Integer getVersion() {
		return null;
	}

	public Integer getEmployeeClinicId() {
		return employeeClinicId;
	}

	public void setEmployeeClinicId(Integer employeeClinicId) {
		this.employeeClinicId = employeeClinicId;
	}

	public String getLastLocalizationAt() {
		return lastLocalizationAt;
	}

	public void setLastLocalizationAt(String lastLocalizationAt) {
		this.lastLocalizationAt = lastLocalizationAt;
	}

	public String getPreferredPhoneNumber() {
		return preferredPhoneNumber;
	}

	public void setPreferredPhoneNumber(String preferredPhoneNumber) {
		this.preferredPhoneNumber = preferredPhoneNumber;
	}

	public String getSupportContactName() {
		return supportContactName;
	}

	public void setSupportContactName(String supportContactName) {
		this.supportContactName = supportContactName;
	}

	public String getSupportContactPhoneNumber() {
		return supportContactPhoneNumber;
	}

	public void setSupportContactPhoneNumber(String supportContactPhoneNumber) {
		this.supportContactPhoneNumber = supportContactPhoneNumber;
	}

	public String getLastActivityAt() {
		return lastActivityAt;
	}

	public void setLastActivityAt(String lastActivityAt) {
		this.lastActivityAt = lastActivityAt;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}
}