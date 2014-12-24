ClipboardPlusPlus
=================

ClipboardPlusPlus is an app to maintain clipboard history and to sync them across devices just by Copy and Paste actions.
The app uses AWS resources and Google Cloud 
Messaging Service in achieving its purpose. User will have to install the device specific 
Clipboard++ app for each of his devices. Detailed explanation of the architecture is as 
below: 
Installation: On installation, the app contacts Google Cloud Messaging (GCM) and 
registers the device. The GCM component gives a unique registration ID upon 
successful registration, and this ID is stored as a SharedPreference key in the local 
device and also an entry will be made in the MySQL DB on cloud in the device table. 
This entry will have the device ID (combination of Android ID and IMEI number), email 
ID(from the android account), GCM sent registration ID and the device description. This 
process happens only once per installation. 
Copy Detect Service: is a background service that has a listener setup on the 
Android's Clipboard Service, so whenever text is copied in the mobile device, the 
Clipboard service notifies the listener and gives the clip that is copied. The service then  
stores the clip into device's SQLite database and also makes a call to the web service 
running in AWS Elastic Beanstalk. For the details on the web service calls refer to the 
Web Service section. The web service then stores the clip in the MySQL database 
(AWS RDS), and also with the help of device ID gets all the registered devices against 
that user and notifies all the other devices using GCM service. The GCM sends a Push 
Notification to all the devices, which will also have the clip content. 
The Broadcast Receiver on the mobile device receives the push notification and stores 
the clip into its SQLite database. 
SyncClipsService: This is an intent service that updates the cloud DB with all the clips 
that were copied when the device did not have the network connectivity. This is listening 
to the network access state, which will be notified every time there is a change in 
network connection. When the network connection is back on, this service gets all the 
clips from the device's SQLite database, that were copied after the 
LastUpdateDateTIme SharedPreference key. 
Shared Preferences: Two keys are being used that are stored in different shared 
preference files: 
  LastUpdatedDateTime: This will be updated with datetime, everytime a clip is 
successfully sent to cloud. This key will be used by ClipSyncService. 
  RegistrationID: Contains the GCM registration ID 
SQLite Database: This component holds all the clip contents (Details in Database 
section). 
Clipboard++ User interface will fetch the clip contents from the local database every 
time the activity is resumed or created. 
