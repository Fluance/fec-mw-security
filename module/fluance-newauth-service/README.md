# Security / user service

## Using

### Requirements:

	JDK 1.8 or later
	Tomcat 8
	

### User profile

#### Create
	curl -v -k https://localhost:8443/user/profile -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"grants\":{\"companies\":[{\"id\":2,\"staff_id\":2000,\"patientunits\":[{\"code\":\"1\"},{\"code\":\"2\"},{\"code\":\"2R\"}],\"code\":\"PKL\",\"hospservices\":[{\"code\":\"S1\"},{\"code\":\"S2\"},{\"code\":\"TAKL\"},{\"code\":\"RES\"},{\"code\":\"PWIT\"},{\"code\":\"THER\"}]}],\"roles\":[\"sysadmin\"]}}"

#### Read
	curl -v -k https://localhost:8443/user/profile/localtestuser1/fluance
	
#### Delete
	curl -v -k https://localhost:8443/user/profile/localtestuser1/fluance -X DELETE
	
#### Grant company hospservices
	curl -v -k https://localhost:8443/user/profile/hospservices/grant -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"hospservices\":[\"IPS\",\"AMBI\",\"PBAU\"]}"
	
#### Set company hospservices
	curl -v -k https://localhost:8443/user/profile/hospservices/set -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"hospservices\":[\"IPS\",\"AMBI\",\"PBAU\"]}"
	
#### Grant company patientunits
	curl -v -k https://localhost:8443/user/profile/patientunits/grant -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"patientunits\":[\"IP\",\"P3\",\"P4\"]}"
	
#### Set company patientunits
	curl -v -k https://localhost:8443/user/profile/patientunits/set -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"patientunits\":[\"IP\",\"P3\",\"P4\"]}"
	
#### Grant company(ies)
	curl -v -k https://localhost:8443/user/profile/companies/grant -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companies\":[{\"companyid\":1,\"staff_id\":5060,\"patientunits\":[\"08\",\"09\",\"10\",\"CH\",\"DI\"],\"hospservices\":[\"1INF\",\"1GYN\",\"1MGE\",\"1MIN\",\"1NEP",\"1ORT\"]},{\"companyid\":3,\"staff_id\":37,\"patientunits\":[\"01\",\"02\",\"03\"],\"hospservices\":[\"INF\",\"GOD2\",\"GOD3\",\"GOFI\"]}]}"
	
#### Set company(ies)
	curl -v -k https://localhost:8443/user/profile/companies/grant -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companies\":[{\"companyid\":3,\"staff_id\":37,\"patientunits\":[\"01\",\"02\",\"03\"],\"hospservices\":[\"INF\",\"GOD2\",\"GOD3\",\"GOFI\"]}]}"
	
#### Grant role(s)	
	curl -v -k https://localhost:8443/user/profile/roles/grant -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"roles\":[\"administrative\",\"physician\"]}"
	
#### Set role(s)
	curl -v -k https://localhost:8443/user/profile/roles/set -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"roles\":[\"nurse\",\"physician\"]}"
	
#### Revoke role(s)	
	curl -v -k https://localhost:8443/user/profile/roles/revoke -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"roles\":[\"physician\"]}"

#### Revoke company hospservices
	curl -v -k https://localhost:8443/user/profile/hospservices/revoke -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"hospservices\":[\"AMBI\",\"PBAU\"]}"
	
#### Revoke company patientunits
	curl -v -k https://localhost:8443/user/profile/patientunits/revoke -H "Content-Type: application/json" -d "{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"patientunits\":[\"P4\"]}"
