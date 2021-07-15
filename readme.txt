You need to have Apache Maven installed

You need to download eBay Java SDK and Finding Kit (https://developer.ebay.com/tools/sdks), and then you need to install ebaycalls.jar and ebaysdkcore.jar from eBay Java SDK
and finding.jar from Finding Kit as artifacts into local Maven repository(global repository doesn't contain them)

---------------------------------------------------------------------------------------------------------------------------------------

Je potrebnÈ maù nainötalovan˝ Apache Maven

Je potrebnÈ stiahn˙ù eBay Java SDK a Finding Kit (https://developer.ebay.com/tools/sdks), a n·sledne nainötalovaù ebaycalls.jar a ebaysdkcore.jar z eBay Java SDK
a finding.jar z Finding Kit ako artefakty do lok·lneho Maven repozit·ru(glob·lny repozit·r ich neobsahuje)

---------------------------------------------------------------------------------------------------------------------------------------

mvn install:install-file -Dfile=finding.jar -DgroupId=com.ebay.services.finding -DartifactId=findingKit -Dpackaging=jar -Dversion=1.0
mvn install:install-file -Dfile=ebaycalls.jar -DgroupId=com.ebay.sdk -DartifactId=ebayCalls -Dpackaging=jar -Dversion=1.065
mvn install:install-file -Dfile=ebaysdkcore.jar -DgroupId=com.ebay.sdk.call -DartifactId=ebaySDKCore -Dpackaging=jar -Dversion=1.065

Folder 'src' contains implementation
File 'pom.xml' is Project Object Model file for Maven dependencies

----------------------------------------------------------------------------------------------------------------------------------------

Zloûka 'src' obsahuje implement·ciu
S˙bor'pom.xml' je Project Object Model s˙bor pre Maven z·vislosti

----------------------------------------------------------------------------------------------------------------------------------------

For application to properly work you need to fill in all the details for application.properties
and service_account.json files in 'src/main/resources' folder.

----------------------------------------------------------------------------------------------------------------------------------------

Pre spr·vne fungovanie aplik·ciu je potrebnÈ vyplniù detaily v s˙boroch application.properties
aservice_account.json v zloûke 'src/main/resources'.

-----------------------------------------------------------------------------------------------------------------------------------------

