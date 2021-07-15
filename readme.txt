You need to have Apache Maven installed

You need to download eBay Java SDK and Finding Kit (https://developer.ebay.com/tools/sdks), and then you need to install ebaycalls.jar and ebaysdkcore.jar from eBay Java SDK
and finding.jar from Finding Kit as artifacts into local Maven repository(global repository doesn't contain them)

---------------------------------------------------------------------------------------------------------------------------------------

mvn install:install-file -Dfile=finding.jar -DgroupId=com.ebay.services.finding -DartifactId=findingKit -Dpackaging=jar -Dversion=1.0
mvn install:install-file -Dfile=ebaycalls.jar -DgroupId=com.ebay.sdk -DartifactId=ebayCalls -Dpackaging=jar -Dversion=1.065
mvn install:install-file -Dfile=ebaysdkcore.jar -DgroupId=com.ebay.sdk.call -DartifactId=ebaySDKCore -Dpackaging=jar -Dversion=1.065

Folder 'src' contains implementation
File 'pom.xml' is Project Object Model file for Maven dependencies

----------------------------------------------------------------------------------------------------------------------------------------

For application to properly work you need to fill in all the details for application.properties
and service_account.json files in 'src/main/resources' folder.

----------------------------------------------------------------------------------------------------------------------------------------
