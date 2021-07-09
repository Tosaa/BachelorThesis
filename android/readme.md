# Android App
This is an app, which can be used to read Data from a ble Server.
Therefore the app is able to scan for advertising devices, connect to them and read the services (discovery process).

If the server is available, it should appear in the list of devices, which can be used to interact with.  
\+ The app is supposed to connect to multiple servers at a time. Therefore this could be analysed aswell. (had no capacity for this :( )  
\- The valid devices are hardcoded at the moment. Generic devices would have complicate the developing process.  

### Android specific information:
- Hilt is used as DI-Framework
- Databinding is used to ease work
- No Bluetooth Library was used, to get deeper insight
- Timber was used for logging
- Used Material Design Components to improve layout

### Complications
In the Android Api the Connection-Interval can not be changed unbound.  
=> Only 3 different options available.
