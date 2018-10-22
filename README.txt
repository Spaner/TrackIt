Track It!
=========

Track It! is a GPS-enabled application for visualizing and planning
outdoor sports and activities. It is written in Java (version 1.7).


Dependencies
============

	Java				>= 1.7
	httpclient 			>= 4.2.3
	log4j				>= 1.2.17
	junit 				>= 4.11
	fit					>= 5.0.0 *
	orange-extensions	>= 1.3.0 *
	geohash-java		>= 1.0.6 *
	
Dependencies marked with * are not available from the Maven Central Repository.
In order to satisfy these dependencies, we suggest installing them on the local
repository with the following maven command:

mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>

Like this:
	
mvn install:install-file -Dfile=<project_base_dir>/src/main/lib/fit.jar -DgroupId=com.garmin -DartifactId=fit -Dversion=5.0.0 -Dpackaging=jar

mvn install:install-file -Dfile=<project_base_dir>/src/main/lib/orange-extensions.1.3.0.jar -DgroupId=com.apple.eawf -DartifactId=orange-extensions -Dversion=1.3.0 -Dpackaging=jar

mvn install:install-file -Dfile=<project_base_dir>/src/main/lib/ch.hsr.geohash-1.0.6.jar -DgroupId=ch.hsr -DartifactId=geohash-java -Dversion=1.0.6 -Dpackaging=jar
    
Needed Keys / Tokens
====================

In order to use some of the map providers, it is necessary to supply a key / token.

- Bing Maps: fill constant value on file BingMapsProvider.java with the appropriate token
  + private static final String token = "";
  
- Bing Elevation Service: fill constant value on file Elevation.java with the appropriate token
  + private static final String API_KEY = "";
  
- OVI Maps: fill constant value on file OVIMapsProvider.java with the appropriate value / token
  + private static final String APP_ID = "";
  +	private static final String APP_TOKEN = "";
  
Important Notice: the Portuguese Survey Maps are not bundled with Track It!
Track It! only offers support for visualizing these maps.
The Portuguese Survey Maps are copyright of Instituto Geográfico do Exército. 


Building from Maven
====================

To build Track It! from source with Maven, execute from the root directory:

mvn clean install

To package the application:

mvn package

Changing version no
====================

a) In Eclipse, edit pom.xml, change the Version field
b) Edit henriquemalheiro/trackit/business/common/Constants.java and change the value of APP_VERSION


Failed connection to SSL Services due to "PKIX Path Building Failed"
=====================================================================
(Most likely an untrusted self-signed certificate!)

First, make sure that your JVM certificate store needs updating.

Try this
1. Download Java class SSLPOKE from 
  https://confluence.atlassian.com/kb/files/779355358/779355357/1/1441897666313/SSLPoke.class
2. Run it from the command line
  			java SSLPOKE <server> 443
  where <server> is the suspected server (eg: opentopomap.org)
3. It the certificate is installed you get the message
  			Successfully connected
  and need no further steps.
  However, expect it to fail because  it is almost certain that a porper certificate is not installed.
  
Most likely, the certificate must be installed. Follow the steps below.

1. Obtain the certificate. Issue the following command from the command line
			openssl s_client -connect <server>:443
  where <server> is the server name (eg: opentopomap.org)
2. A section of the output from the command should show lines begin at 
			-----BEGIN CERTIFICATE-----
  and ending at 
			-----END CERTIFICATE-----
3. Open a text file and save to it all lines between the two lines above, INCLUDING the two lines.
   Save the file as certificate.crt (or any other name, if you like)
4. From the command line run
			<JAVA_HOME>/bin/keytool -import -alias <server> -keystore <JAVA_HOME>/jre/lib/security/cacerts -file certificate.crt
  (where <JAVA_HOME> is your Java installation home directory)
  and you are done installing the missing certificate.

