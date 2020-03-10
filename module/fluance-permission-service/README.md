# Security / permission service

## Using

### Requirements:

	JDK 1.8 or later
	
### Permission service application

To deploy XACML policies, just copy the conf folder at the root of any folder in the application's classpath.
/conf
	xacml-pdp-config.xml
	/xamcml-policies
		MyPolicy1.xml
		MyPolicy2.xml
		...
		MyPolicyN.xml
		
This are the expected JV arguments, specially in local environments:
-Djavax.net.ssl.trustStoreType="JKS"
-Djavax.net.ssl.trustStorePassword="fluance"
-Djavax.net.ssl.trustStore="\security\truststore.jks" -->Set yout own

The project contains policies folder with examples of the policies in different environments.
