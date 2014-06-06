# How to Enable Notifications in your Application

**Step 1:** In your `app.json`, add `"notifications" : true` to the top level object. For example:


    {
      "appFormatVersion": 5,
      "notifications" : true,
      "rootPage": {
    ...


**Step 2:** In the `<manifest>` (top-level) block of your AndroidManifest.xml, add the following permissions:


    <permission android:name="your_package.permission.C2D_MESSAGE"
                android:protectionLevel="signature" />
    <uses-permission android:name="your_package.permission.C2D_MESSAGE" />


Replace `your_package` (in both places) with your application's package name.

**Step 3:** In the `<application>` block of your AndroidManifest.xml, add the following:


        <receiver
            android:name="com.reachlocal.mobile.liger.gcm.LigerGcmBroadcastReceiver"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="your_package." />
            </intent-filter>
        </receiver>

        <service android:name="com.reachlocal.mobile.liger.gcm.LigerGcmIntentService" />

Again, replace `your_package` with your application's package name.

**Step 4:** Use the Google Play Developer Console (https://code.google.com/apis/console/?hl=bn) to obtain your Google Cloud Messaging
Sender ID.


**Step 5:** In your `strings.xml` file (in `src/main/res/values/strings.xml`) add the following string with the appropriate value for `sender_id`:


    <string name="liger_gcm_sender_id">sender_id</string>


**Step 6 (optional):** If you have created a custom subclass of `DefaultMainActivity`, you will need to add a string with
your main activity class name:


    <string name="liger_main_activity_class">your_main_activity</string>


