#
##
##
####Sample user profile JSON
	{"username":"fluancetestuser2","domain":"corporate","grants":{"grantedCompanies":[{"id":2,"patientunits":[{"code":"1"},{"code":"2"},{"code":"2R"},{"code":"IP"},{"code":"P3"},{"code":"P4"}],"code":"PKL","hospservices":[{"code":"S1"},{"code":"S2"},{"code":"TAKL"},{"code":"RES"},{"code":"IPS"},{"code":"PWIT"},{"code":"PBAU"},{"code":"AMBI"},{"code":"THER"}]}],"roles":[]}}
##### For cURL
	{\"username\":\"fluancetestuser2\",\"domain\":\"corporate\",\"grants\":{\"grantedCompanies\":[{\"id\":2,\"patientunits\":[{\"code\":\"1\"},{\"code\":\"2\"},{\"code\":\"2R\"},{\"code\":\"IP\"},{\"code\":\"P3\"},{\"code\":\"P4\"}],\"code\":\"PKL\",\"hospservices\":[{\"code\":\"S1\"},{\"code\":\"S2\"},{\"code\":\"TAKL\"},{\"code\":\"RES\"},{\"code\":\"IPS\"},{\"code\":\"PWIT\"},{\"code\":\"PBAU\"},{\"code\":\"AMBI\"},{\"code\":\"THER\"}]}],\"roles\":[]}}
	
#### Sample company grant payload
{\"username\":\"localtestuser1\",\"domain\":\"fluance\",\"companyid\":2,\"hospservices\":[\"IP\",\"AMBI\",\"PBAU\"]}