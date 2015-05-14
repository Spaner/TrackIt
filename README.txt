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
