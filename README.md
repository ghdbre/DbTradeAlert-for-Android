<img src="https://cloud.githubusercontent.com/assets/17050078/18308576/6270e068-74f5-11e6-845e-aa41b694496d.png" align="left" />

# DbTradeAlert for Android
DbTradeAlert alerts you to securities reaching a specified price or at a specified date. 

See ...\app\src\main\assets\Help.html for a detailed descripion.

Note that you need to provide additional files:
* a suitable ...\app\google-services.json to build the playStore and withAds flavors
* a AndroidKeyStore.jks file specified in the keystore path when generating the signed APK
* a "$project.rootDir/../../DbTradeAlert/project.properties" file containing "ad_unit_id=<your-ad-unit-id" to use the withAds flavor
