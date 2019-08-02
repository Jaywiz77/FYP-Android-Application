package com.io.sis.development;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import static com.io.sis.development.App.CHANNEL_ID;

public class ForegroundService extends Service implements BeaconConsumer {

    Handler handler;
    ApplicationBroadcastReceiver broadcastReceiver;
    LocationManager locationManager ;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHelper db;
    String participantPin;
    String applicationName = "IO.SIS_Dev";
    String runningStatus = "Running";
    String stoppedStatus = "Stopped";

    private BeaconManager mBeaconManager;
    private BackgroundPowerSaver backgroundPowerSaver;

    private boolean houseBeaconDetailsRecorded = false;
    String houseBeaconStartTime = "";
    String houseBeaconEndTime = "";
    int houseBeaconUniqueID = 0;

    private boolean hospitalBeaconDetailsRecorded = false;
    String hospitalBeaconStartTime = "";
    String hospitalBeaconEndTime = "";
    int hospitalBeaconUniqueID = 0;

    String rangingHouseId = "rangingHouseId";
    String rangingHospitalId = "rangingHospitalId";
    String monitoringHouseId = "monitoringHouseId";
    String monitoringHospitalId = "monitoringHospitalId";

    String serviceStartTime = "";
    String serviceEndTime = "";
    int serviceEventUniqueID = 0;

    boolean isScreenOn = true;
    boolean screenOffRecorded = false;
    boolean screenOnRecorded = false;
    String screenStartTime = "";
    String screenEndTime = "";
    int screenEventUniqueID = 0;

    String facebookPackageName = "com.facebook.katana";
    boolean facebookAppCurrentlyOpened = false;
    boolean facebookAppCloseRecorded = false;
    String facebookAppStartTime = "";
    String facebookAppEndTime = "";
    int facebookAppEventUniqueID = 0;

    String youtubePackageName = "com.google.android.youtube";
    boolean youtubeAppCurrentlyOpened = false;
    boolean youtubeAppCloseRecorded = false;
    String youtubeAppStartTime = "";
    String youtubeAppEndTime = "";
    int youtubeAppEventUniqueID = 0;

    String linePackageName = "jp.naver.line.android";
    boolean lineAppCurrentlyOpened = false;
    boolean lineAppCloseRecorded = false;
    String lineAppStartTime = "";
    String lineAppEndTime = "";
    int lineAppEventUniqueID = 0;

    String siamCommercialBankPackageName = "com.scb.phone";
    boolean scbEasyAppCurrentlyOpened = false;
    boolean scbEasyAppCloseRecorded = false;
    String scbEasyAppStartTime = "";
    String scbEasyAppEndTime = "";
    int scbEasyAppEventUniqueID = 0;

    String kasikornBankPackageName = "com.kasikorn.retail.mbanking.wap";
    boolean kPlusAppCurrentlyOpened = false;
    boolean kPlusAppCloseRecorded = false;
    String kPlusAppStartTime = "";
    String kPlusAppEndTime = "";
    int kPlusAppEventUniqueID = 0;

    String bangkokBankPackageName = "com.bbl.mobilebanking";
    boolean bualuangmBankingAppCurrentlyOpened = false;
    boolean bualuangmBankingAppCloseRecorded = false;
    String bualuangmBankingAppStartTime = "";
    String bualuangmBankingAppEndTime = "";
    int bualuangmBankingAppEventUniqueID = 0;

    boolean lineCallStartRecorded = false;
    boolean lineCallEndRecorded = false;
    boolean lineCallInProgress = false;
    String lineCallStartTime = "";
    String lineCallEndTime = "";
    int lineCallEventUniqueID = 0;

    boolean phoneCallStartRecorded = false;
    boolean phoneCallEndRecorded = false;
    String phoneCallStartTime = "";
    String phoneCallEndTime = "";
    int phoneCallEventUniqueID = 0;

    boolean phoneChargeStartRecorded = false;
    boolean phoneChargeEndRecorded = false;
    String phoneChargeStartTime = "";
    String phoneChargeEndTime = "";
    int phoneChargeEventUniqueID = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra("inputExtra");

        /*** Start of foreground service ***/
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //.setContentTitle(applicationName + " services are currently running")
                .setContentText(applicationName + " services are currently running")
                .setSmallIcon(R.mipmap.ic_app_logo)
                /*** set ContentIntent allows our app to open when notification is pressed ***/
                //.setContentIntent(pendingIntent)
                .build();

        /*** Without startForeground, our app will only run for 5 seconds and Android will kill our service ***/
        startForeground(1, notification);

        /*** Register screen on/off listeners ***/
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        broadcastReceiver = new ApplicationBroadcastReceiver();
        registerReceiver(broadcastReceiver,filter);

        /*** Set up database ***/
        db = new DatabaseHelper(this);

        /*** Retrieve stored participantPin from sharedPreferences ***/
        pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
        participantPin = pref.getString("participantPin", "0");

        /*** Set up beacon parameters ***/
        mBeaconManager = BeaconManager.getInstanceForApplication(this);
        mBeaconManager.getBeaconParsers().clear();
        // Detect the main identifier (UID) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        //mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        //This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        //mBeaconManager.enableForegroundServiceScanning(notification, 456);
        mBeaconManager.setEnableScheduledScanJobs(false);
        mBeaconManager.setBackgroundMode(true);
        mBeaconManager.setBackgroundScanPeriod(500);
        mBeaconManager.setBackgroundBetweenScanPeriod(500);
        mBeaconManager.setForegroundScanPeriod(500);
        mBeaconManager.setForegroundBetweenScanPeriod(500);

        mBeaconManager.bind(this);
        //mBeaconManager.setDebug(true);

        /*** Save serving running status to SharedPreferences ***/
        updateSharedPreferencesValue(getApplicationContext(), "serviceRunningStatus", "serviceRunning");

        /*** Record that service is running to SQLite ***/
        if(serviceEventUniqueID == 0){
            serviceEventUniqueID = getUniqueNumber();
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            serviceStartTime = dateFormat.format(date);
            boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Service", serviceStartTime, "-", serviceEventUniqueID, runningStatus, "-");
            if(isInserted){
                updateSharedPreferencesValue(getApplicationContext(), "serviceEventUniqueID", String.valueOf(serviceEventUniqueID));
                Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
            }
        }

        /*** do heavy work on a background thread, call stopSelf() when all work is done
         for our scenario, our work will never be done so don't have to call ***/
        //stopSelf();

        /*** Run a thread continuously every second or 1000 milliseconds ***/
        final int timeInMilliseconds = 1000;
        handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {

                /*** Get current timestamp ***/
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                getScreenStatus();

                String currentApp = getForegroundActivity();
                String closedApp = getBackgroundActivity();

                /*** Check if bluetooth is disabled, auto enable bluetooth ***/
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                boolean isEnabled = bluetoothAdapter.isEnabled();
                if (!isEnabled) {
                    bluetoothAdapter.enable();
                }
                /*** Check if location service is turned on/enabled ***/
                if(!hasLocationServiceEnabled()){
                    stopServiceAsRequirementsNotMet("Location Service(GPS) Disabled");
                }
                /*** Check if coarse location permission is revoked ***/
                if(!hasCoarseLocationPermission()){
                    stopServiceAsRequirementsNotMet("Coarse Location Permission Revoked");
                }

                /*** Check if phone is charging ***/
                boolean phoneChargeBoolean = isPhoneCharging(getApplicationContext());
                if(phoneChargeBoolean){
                    if(!phoneChargeStartRecorded){
                        Toast.makeText(getApplicationContext(), "Phone is charging", Toast.LENGTH_LONG).show();
                        String notificationMessage = "Phone started charging at " + dateFormat.format(date);
                        displayNotification(applicationName, notificationMessage);
                        phoneChargeStartRecorded = true;
                        phoneChargeEndRecorded = false;
                        if(phoneChargeStartTime.equals("") && phoneChargeEndTime.equals("") && phoneChargeEventUniqueID == 0){
                            phoneChargeStartTime = dateFormat.format(date);
                            phoneChargeEventUniqueID = getUniqueNumber();
                            boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Charge", phoneChargeStartTime, "-", phoneChargeEventUniqueID, runningStatus, "-");
                            if(isInserted){
                                updateSharedPreferencesValue(getApplicationContext(), "phoneChargeEventUniqueID", String.valueOf(phoneChargeEventUniqueID));
                                Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else{
                    if(!phoneChargeEndRecorded && phoneChargeStartRecorded){
                        Toast.makeText(getApplicationContext(), "Phone charge ended", Toast.LENGTH_LONG).show();
                        String notificationMessage = "Phone charge ended at " + dateFormat.format(date);
                        displayNotification(applicationName, notificationMessage);
                        phoneChargeEndRecorded = true;
                        phoneChargeStartRecorded = false;
                        if(!phoneChargeStartTime.equals("") && phoneChargeEndTime.equals("") && phoneChargeEventUniqueID!=0){
                            phoneChargeEndTime = dateFormat.format(date);
                            Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneChargeEndTime, phoneChargeEventUniqueID, stoppedStatus, "-");
                            if(numberOfRowsUpdated > 0){
                                phoneChargeStartTime = "";
                                phoneChargeEndTime = "";
                                phoneChargeEventUniqueID = 0;
                                updateSharedPreferencesValue(getApplicationContext(), "phoneChargeEventUniqueID", String.valueOf(phoneChargeEventUniqueID));
                                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                /*** Check if line call is in progress ***/
                /*** Works for line voice call and video call ***/
                boolean lineCallBoolean = isLineCallInProgress(getApplicationContext());
                if(lineCallBoolean){
                    if(!lineCallStartRecorded){
                        if(currentApp.equals(linePackageName)){
                            Toast.makeText(getApplicationContext(), "Line VoIP call is in progress", Toast.LENGTH_LONG).show();
                            String notificationMessage = "Line VoIP call started at " + dateFormat.format(date);
                            lineCallInProgress = true;
                            lineCallStartRecorded = true;
                            lineCallEndRecorded = false;
                            displayNotification(applicationName, notificationMessage);
                            if(lineCallStartTime.equals("") && lineCallEndTime.equals("") && lineCallEventUniqueID == 0){
                                lineCallStartTime = dateFormat.format(date);
                                lineCallEventUniqueID = getUniqueNumber();
                                boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Line_Call", lineCallStartTime, "-", lineCallEventUniqueID, runningStatus, "-");
                                if(isInserted){
                                    updateSharedPreferencesValue(getApplicationContext(), "lineCallEventUniqueID", String.valueOf(lineCallEventUniqueID));
                                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
                else{
                    if(!lineCallEndRecorded && lineCallStartRecorded && lineCallInProgress){
                        Toast.makeText(getApplicationContext(), "Line VoIP call ended", Toast.LENGTH_LONG).show();
                        String notificationMessage = "Line VoIP call ended at " + dateFormat.format(date);
                        displayNotification(applicationName, notificationMessage);
                        lineCallEndRecorded = true;
                        lineCallStartRecorded = false;
                        lineCallInProgress = false;
                        if(!lineCallStartTime.equals("") && lineCallEndTime.equals("") && lineCallEventUniqueID!=0){
                            lineCallEndTime = dateFormat.format(date);
                            Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineCallEndTime, lineCallEventUniqueID, stoppedStatus, "-");
                            if(numberOfRowsUpdated > 0){
                                lineCallStartTime = "";
                                lineCallEndTime = "";
                                lineCallEventUniqueID = 0;
                                updateSharedPreferencesValue(getApplicationContext(), "lineCallEventUniqueID", String.valueOf(lineCallEventUniqueID));
                                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                /*** Check if phone call is in progress ***/
                boolean phoneCallBoolean = isPhoneCallInProgress(getApplicationContext());
                if(phoneCallBoolean){
                    if(!phoneCallStartRecorded){
                        Toast.makeText(getApplicationContext(), "Phone call is in progress", Toast.LENGTH_LONG).show();
                        String notificationMessage = "Phone call started at " + dateFormat.format(date);
                        displayNotification(applicationName, notificationMessage);
                        phoneCallStartRecorded = true;
                        phoneCallEndRecorded = false;
                        if(phoneCallStartTime.equals("") && phoneCallEndTime.equals("") && phoneCallEventUniqueID == 0){
                            phoneCallStartTime = dateFormat.format(date);
                            phoneCallEventUniqueID = getUniqueNumber();
                            boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Phone_Call", phoneCallStartTime, "-", phoneCallEventUniqueID, runningStatus, "-");
                            if(isInserted){
                                updateSharedPreferencesValue(getApplicationContext(), "phoneCallEventUniqueID", String.valueOf(phoneCallEventUniqueID));
                                Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else{
                    if(!phoneCallEndRecorded && phoneCallStartRecorded){
                        Toast.makeText(getApplicationContext(), "Phone call ended", Toast.LENGTH_LONG).show();
                        String notificationMessage = "Phone call ended at " + dateFormat.format(date);
                        displayNotification(applicationName, notificationMessage);
                        phoneCallEndRecorded = true;
                        phoneCallStartRecorded = false;
                        if(!phoneCallStartTime.equals("") && phoneCallEndTime.equals("") && phoneCallEventUniqueID!=0){
                            phoneCallEndTime = dateFormat.format(date);
                            Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneCallEndTime, phoneCallEventUniqueID, stoppedStatus, "-");
                            if(numberOfRowsUpdated > 0){
                                phoneCallStartTime = "";
                                phoneCallEndTime = "";
                                phoneCallEventUniqueID = 0;
                                updateSharedPreferencesValue(getApplicationContext(), "phoneCallEventUniqueID", String.valueOf(phoneCallEventUniqueID));
                                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                /*** Check if phone screen is on/off ***/
                if(isScreenOn){
                    screenOffRecorded = false;
                    if(!screenOnRecorded){
                        String notificationMessage = "Screen turned on at " + dateFormat.format(date);
                        //displayNotification(applicationName, notificationMessage);
                        screenOnRecorded = true;
                        if(screenStartTime.equals("") && screenEndTime.equals("") && screenEventUniqueID == 0){
                            screenStartTime = dateFormat.format(date);
                            screenEventUniqueID = getUniqueNumber();
                            boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Screen", screenStartTime, "-", screenEventUniqueID, runningStatus, "-");
                            if(isInserted){
                                updateSharedPreferencesValue(getApplicationContext(), "screenEventUniqueID", String.valueOf(screenEventUniqueID));
                                Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else if(screenOnRecorded) {
                        getAppStatus(applicationName, "", currentApp, closedApp);
                    }
                }
                // Screen is off
                else{
                    if(!screenOffRecorded){
                        if(screenOnRecorded){
                            String notificationMessage = "Screen turned off at " + dateFormat.format(date);
                            //displayNotification(applicationName, notificationMessage);
                            screenOnRecorded = false;
                            if(!screenStartTime.equals("") && screenEndTime.equals("") && screenEventUniqueID!=0){
                                screenEndTime = dateFormat.format(date);
                                Integer numberOfRowsUpdated = db.updatePhoneUsageData(screenEndTime, screenEventUniqueID, stoppedStatus, "-");
                                if(numberOfRowsUpdated > 0){
                                    screenStartTime = "";
                                    screenEndTime = "";
                                    screenEventUniqueID = 0;
                                    updateSharedPreferencesValue(getApplicationContext(), "screenEventUniqueID", String.valueOf(screenEventUniqueID));
                                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        else{
                            updateAppClosureUponScreenOff(currentApp, dateFormat.format(date));
                        }
                    }
                }
                handler.postDelayed(this, timeInMilliseconds);
            }
        };
        runnable.run();

        // return either START_STICKY, START_NOT_STICKY or START_REDELIVER_INTENT
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Stopping...", Toast.LENGTH_SHORT).show();
        handler.removeMessages(0);
        unregisterReceiver(broadcastReceiver);
        updateSharedPreferencesValue(getApplicationContext(), "serviceRunningStatus", "serviceStopped");
//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
//        editor = pref.edit();
//        editor.clear();
//        editor.apply();

        Log.e(applicationName, "SERVICE GOT DESTROYED");
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.removeAllMonitorNotifiers();
        try {
            mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHouseId, null, null, null));
            mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHospitalId, null, null, null));
            mBeaconManager.stopMonitoringBeaconsInRegion(new Region(monitoringHouseId, Identifier.parse("0xc8251d848e225e4d6952"), Identifier.parse("0xabcde0ab00c6"), null));
            mBeaconManager.stopMonitoringBeaconsInRegion(new Region(monitoringHospitalId, Identifier.parse("0xc8251d848e225e4d6952"), Identifier.parse("0xabcde0a20030"), null));
        } catch (RemoteException e) {   }
        mBeaconManager.unbind(this);

        updateRunningComponentsToStopped();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getAppStatus(String title, String notificationMessage, String currentApp, String closedApp) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        /*** Facebook App ***/
        if(currentApp.equals(facebookPackageName)) {
            // new open
            if (!facebookAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!facebookAppCloseRecorded) {
                    facebookAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(facebookAppStartTime.equals("") && facebookAppEndTime.equals("") && facebookAppEventUniqueID == 0){
                        facebookAppStartTime = dateFormat.format(date);
                        facebookAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Facebook", facebookAppStartTime, "-", facebookAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "facebookAppEventUniqueID", String.valueOf(facebookAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(facebookAppCloseRecorded) {
                    facebookAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(facebookAppStartTime.equals("") && facebookAppEndTime.equals("") && facebookAppEventUniqueID == 0){
                        facebookAppStartTime = dateFormat.format(date);
                        facebookAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Facebook", facebookAppStartTime, "-", facebookAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "facebookAppEventUniqueID", String.valueOf(facebookAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(facebookPackageName) && closedApp.equals(facebookPackageName)){
            // close/minimize
            if(facebookAppCurrentlyOpened){
                // not recorded
                if(!facebookAppCloseRecorded){
                    facebookAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!facebookAppStartTime.equals("") && facebookAppEndTime.equals("") && facebookAppEventUniqueID!=0){
                        facebookAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(facebookAppEndTime, facebookAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            facebookAppStartTime = "";
                            facebookAppEndTime = "";
                            facebookAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "facebookAppEventUniqueID", String.valueOf(facebookAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /*** Youtube App ***/
        if(currentApp.equals(youtubePackageName)) {
            // new open
            if (!youtubeAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!youtubeAppCloseRecorded) {
                    youtubeAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(youtubeAppStartTime.equals("") && youtubeAppEndTime.equals("") && youtubeAppEventUniqueID == 0){
                        youtubeAppStartTime = dateFormat.format(date);
                        youtubeAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Youtube", youtubeAppStartTime, "-", youtubeAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "youtubeAppEventUniqueID", String.valueOf(youtubeAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(youtubeAppCloseRecorded) {
                    youtubeAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(youtubeAppStartTime.equals("") && youtubeAppEndTime.equals("") && youtubeAppEventUniqueID == 0){
                        youtubeAppStartTime = dateFormat.format(date);
                        youtubeAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Youtube", youtubeAppStartTime, "-", youtubeAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "youtubeAppEventUniqueID", String.valueOf(youtubeAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(youtubePackageName) && closedApp.equals(youtubePackageName)){
            // close/minimize
            if(youtubeAppCurrentlyOpened){
                // not recorded
                if(!youtubeAppCloseRecorded){
                    youtubeAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!youtubeAppStartTime.equals("") && youtubeAppEndTime.equals("") && youtubeAppEventUniqueID!=0){
                        youtubeAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(youtubeAppEndTime, youtubeAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            youtubeAppStartTime = "";
                            youtubeAppEndTime = "";
                            youtubeAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "youtubeAppEventUniqueID", String.valueOf(youtubeAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /*** Line App ***/
        if(currentApp.equals(linePackageName)) {
            // new open
            if (!lineAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!lineAppCloseRecorded) {
                    lineAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(lineAppStartTime.equals("") && lineAppEndTime.equals("") && lineAppEventUniqueID == 0){
                        lineAppStartTime = dateFormat.format(date);
                        lineAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Line", lineAppStartTime, "-", lineAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "lineAppEventUniqueID", String.valueOf(lineAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(lineAppCloseRecorded) {
                    lineAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(lineAppStartTime.equals("") && lineAppEndTime.equals("") && lineAppEventUniqueID == 0){
                        lineAppStartTime = dateFormat.format(date);
                        lineAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "Line", lineAppStartTime, "-", lineAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "lineAppEventUniqueID", String.valueOf(lineAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(linePackageName) && closedApp.equals(linePackageName)){
            // close/minimize
            if(lineAppCurrentlyOpened){
                // not recorded
                if(!lineAppCloseRecorded){
                    lineAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!lineAppStartTime.equals("") && lineAppEndTime.equals("") && lineAppEventUniqueID!=0){
                        lineAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineAppEndTime, lineAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            lineAppStartTime = "";
                            lineAppEndTime = "";
                            lineAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "lineAppEventUniqueID", String.valueOf(lineAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /*** SCB Easy App by Siam Commercial Bank ***/
        if(currentApp.equals(siamCommercialBankPackageName)) {
            // new open
            if (!scbEasyAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!scbEasyAppCloseRecorded) {
                    scbEasyAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(scbEasyAppStartTime.equals("") && scbEasyAppEndTime.equals("") && scbEasyAppEventUniqueID == 0){
                        scbEasyAppStartTime = dateFormat.format(date);
                        scbEasyAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "SiamCommercialBank", scbEasyAppStartTime, "-", scbEasyAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "scbEasyAppEventUniqueID", String.valueOf(scbEasyAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(scbEasyAppCloseRecorded) {
                    scbEasyAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(scbEasyAppStartTime.equals("") && scbEasyAppEndTime.equals("") && scbEasyAppEventUniqueID == 0){
                        scbEasyAppStartTime = dateFormat.format(date);
                        scbEasyAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "SiamCommercialBank", scbEasyAppStartTime, "-", scbEasyAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "scbEasyAppEventUniqueID", String.valueOf(scbEasyAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(siamCommercialBankPackageName) && closedApp.equals(siamCommercialBankPackageName)){
            // close/minimize
            if(scbEasyAppCurrentlyOpened){
                // not recorded
                if(!scbEasyAppCloseRecorded){
                    scbEasyAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!scbEasyAppStartTime.equals("") && scbEasyAppEndTime.equals("") && scbEasyAppEventUniqueID!=0){
                        scbEasyAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(scbEasyAppEndTime, scbEasyAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            scbEasyAppStartTime = "";
                            scbEasyAppEndTime = "";
                            scbEasyAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "scbEasyAppEventUniqueID", String.valueOf(scbEasyAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /*** K Plus App by Kasikorn Bank ***/
        if(currentApp.equals(kasikornBankPackageName)) {
            // new open
            if (!kPlusAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!kPlusAppCloseRecorded) {
                    kPlusAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(kPlusAppStartTime.equals("") && kPlusAppEndTime.equals("") && kPlusAppEventUniqueID == 0){
                        kPlusAppStartTime = dateFormat.format(date);
                        kPlusAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "KasikornBank", kPlusAppStartTime, "-", kPlusAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "kPlusAppEventUniqueID", String.valueOf(kPlusAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(kPlusAppCloseRecorded) {
                    kPlusAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(kPlusAppStartTime.equals("") && kPlusAppEndTime.equals("") && kPlusAppEventUniqueID == 0){
                        kPlusAppStartTime = dateFormat.format(date);
                        kPlusAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "KasikornBank", kPlusAppStartTime, "-", kPlusAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "kPlusAppEventUniqueID", String.valueOf(kPlusAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(kasikornBankPackageName) && closedApp.equals(kasikornBankPackageName)){
            // close/minimize
            if(kPlusAppCurrentlyOpened){
                // not recorded
                if(!kPlusAppCloseRecorded){
                    kPlusAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!kPlusAppStartTime.equals("") && kPlusAppEndTime.equals("") && kPlusAppEventUniqueID!=0){
                        kPlusAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(kPlusAppEndTime, kPlusAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            kPlusAppStartTime = "";
                            kPlusAppEndTime = "";
                            kPlusAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "kPlusAppEventUniqueID", String.valueOf(kPlusAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        /*** Bualuang mBanking App by Bangkok Bank ***/
        if(currentApp.equals(bangkokBankPackageName)) {
            // new open
            if (!bualuangmBankingAppCurrentlyOpened) {
                // not recorded and needs to reset, new open
                if (!bualuangmBankingAppCloseRecorded) {
                    bualuangmBankingAppCurrentlyOpened = true;
                    notificationMessage = "Launched " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(bualuangmBankingAppStartTime.equals("") && bualuangmBankingAppEndTime.equals("") && bualuangmBankingAppEventUniqueID == 0){
                        bualuangmBankingAppStartTime = dateFormat.format(date);
                        bualuangmBankingAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "BangkokBank", bualuangmBankingAppStartTime, "-", bualuangmBankingAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "bualuangmBankingAppEventUniqueID", String.valueOf(bualuangmBankingAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            // already opened
            else{
                if(bualuangmBankingAppCloseRecorded) {
                    bualuangmBankingAppCloseRecorded = false;
                    notificationMessage = "New Launch " + currentApp + " at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(bualuangmBankingAppStartTime.equals("") && bualuangmBankingAppEndTime.equals("") && bualuangmBankingAppEventUniqueID == 0){
                        bualuangmBankingAppStartTime = dateFormat.format(date);
                        bualuangmBankingAppEventUniqueID = getUniqueNumber();
                        boolean isInserted = db.insertPhoneUsageData(Integer.parseInt(participantPin), "BangkokBank", bualuangmBankingAppStartTime, "-", bualuangmBankingAppEventUniqueID, runningStatus, "-");
                        if(isInserted){
                            updateSharedPreferencesValue(getApplicationContext(), "bualuangmBankingAppEventUniqueID", String.valueOf(bualuangmBankingAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
        else if(!currentApp.equals(bangkokBankPackageName) && closedApp.equals(bangkokBankPackageName)){
            // close/minimize
            if(bualuangmBankingAppCurrentlyOpened){
                // not recorded
                if(!bualuangmBankingAppCloseRecorded){
                    bualuangmBankingAppCloseRecorded = true;
                    notificationMessage = "Closed " + closedApp +" at\n " + dateFormat.format(date);
                    displayNotification(title, notificationMessage);
                    if(!bualuangmBankingAppStartTime.equals("") && bualuangmBankingAppEndTime.equals("") && bualuangmBankingAppEventUniqueID!=0){
                        bualuangmBankingAppEndTime = dateFormat.format(date);
                        Integer numberOfRowsUpdated = db.updatePhoneUsageData(bualuangmBankingAppEndTime, bualuangmBankingAppEventUniqueID, stoppedStatus, "-");
                        if(numberOfRowsUpdated > 0){
                            bualuangmBankingAppStartTime = "";
                            bualuangmBankingAppEndTime = "";
                            bualuangmBankingAppEventUniqueID = 0;
                            updateSharedPreferencesValue(getApplicationContext(), "bualuangmBankingAppEventUniqueID", String.valueOf(bualuangmBankingAppEventUniqueID));
                            Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    public void updateAppClosureUponScreenOff(String currentApp, String dateTimeValue){
        /*** Facebook App ***/
        if(currentApp.equals(facebookPackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            facebookAppCurrentlyOpened = true;
            facebookAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!facebookAppStartTime.equals("") && facebookAppEndTime.equals("") && facebookAppEventUniqueID!=0){
                facebookAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(facebookAppEndTime, facebookAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    facebookAppStartTime = "";
                    facebookAppEndTime = "";
                    facebookAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "facebookAppEventUniqueID", String.valueOf(facebookAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
        /*** Youtube App ***/
        if(currentApp.equals(youtubePackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            youtubeAppCurrentlyOpened = true;
            youtubeAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!youtubeAppStartTime.equals("") && youtubeAppEndTime.equals("") && youtubeAppEventUniqueID!=0){
                youtubeAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(youtubeAppEndTime, youtubeAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    youtubeAppStartTime = "";
                    youtubeAppEndTime = "";
                    youtubeAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "youtubeAppEventUniqueID", String.valueOf(youtubeAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
        /*** Line App ***/
        if(currentApp.equals(linePackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            lineAppCurrentlyOpened = true;
            lineAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!lineAppStartTime.equals("") && lineAppEndTime.equals("") && lineAppEventUniqueID!=0){
                lineAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineAppEndTime, lineAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    lineAppStartTime = "";
                    lineAppEndTime = "";
                    lineAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "lineAppEventUniqueID", String.valueOf(lineAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*** SCB Easy App by Siam Commercial Bank ***/
        if(currentApp.equals(siamCommercialBankPackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            scbEasyAppCurrentlyOpened = true;
            scbEasyAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!scbEasyAppStartTime.equals("") && scbEasyAppEndTime.equals("") && scbEasyAppEventUniqueID!=0){
                scbEasyAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(scbEasyAppEndTime, scbEasyAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    scbEasyAppStartTime = "";
                    scbEasyAppEndTime = "";
                    scbEasyAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "scbEasyAppEventUniqueID", String.valueOf(scbEasyAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*** K Plus App by Kasikorn Bank ***/
        if(currentApp.equals(kasikornBankPackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            kPlusAppCurrentlyOpened = true;
            kPlusAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!kPlusAppStartTime.equals("") && kPlusAppEndTime.equals("") && kPlusAppEventUniqueID!=0){
                kPlusAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(kPlusAppEndTime, kPlusAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    kPlusAppStartTime = "";
                    kPlusAppEndTime = "";
                    kPlusAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "kPlusAppEventUniqueID", String.valueOf(kPlusAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /*** Bualuang mBanking App by Bangkok Bank ***/
        if(currentApp.equals(bangkokBankPackageName)) {
            String notificationMessage = "Screen off: Closed " + currentApp +" at\n " + dateTimeValue;
            displayNotification(applicationName, notificationMessage);
            bualuangmBankingAppCurrentlyOpened = true;
            bualuangmBankingAppCloseRecorded = true;
            screenOffRecorded = true;
            screenOnRecorded = false;
            if(!bualuangmBankingAppStartTime.equals("") && bualuangmBankingAppEndTime.equals("") && bualuangmBankingAppEventUniqueID!=0){
                bualuangmBankingAppEndTime = dateTimeValue;
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(bualuangmBankingAppEndTime, bualuangmBankingAppEventUniqueID, stoppedStatus, "-");
                if(numberOfRowsUpdated > 0){
                    bualuangmBankingAppStartTime = "";
                    bualuangmBankingAppEndTime = "";
                    bualuangmBankingAppEventUniqueID = 0;
                    updateSharedPreferencesValue(getApplicationContext(), "bualuangmBankingAppEventUniqueID", String.valueOf(bualuangmBankingAppEventUniqueID));
                    Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void updateRunningComponentsToStopped(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
        String serviceStopReason = pref.getString("serviceStopReason", "-");

        if(serviceEventUniqueID!=0){
            serviceEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(serviceEndTime, serviceEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                serviceStartTime = "";
                serviceEndTime = "";
                serviceEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "serviceEventUniqueID", String.valueOf(serviceEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(screenEventUniqueID!=0){
            screenEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(screenEndTime, screenEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                screenStartTime = "";
                screenEndTime = "";
                screenEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "screenEventUniqueID", String.valueOf(screenEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(phoneChargeEventUniqueID!=0){
            phoneChargeEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneChargeEndTime, phoneChargeEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                phoneChargeStartTime = "";
                phoneChargeEndTime = "";
                phoneChargeEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "phoneChargeEventUniqueID", String.valueOf(phoneChargeEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(facebookAppEventUniqueID!=0){
            facebookAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(facebookAppEndTime, facebookAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                facebookAppStartTime = "";
                facebookAppEndTime = "";
                facebookAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "facebookAppEventUniqueID", String.valueOf(facebookAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(youtubeAppEventUniqueID!=0){
            youtubeAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(youtubeAppEndTime, youtubeAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                youtubeAppStartTime = "";
                youtubeAppEndTime = "";
                youtubeAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "youtubeAppEventUniqueID", String.valueOf(youtubeAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(lineAppEventUniqueID!=0){
            lineAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineAppEndTime, lineAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                lineAppStartTime = "";
                lineAppEndTime = "";
                lineAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "lineAppEventUniqueID", String.valueOf(lineAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(scbEasyAppEventUniqueID!=0){
            scbEasyAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(scbEasyAppEndTime, scbEasyAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                scbEasyAppStartTime = "";
                scbEasyAppEndTime = "";
                scbEasyAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "scbEasyAppEventUniqueID", String.valueOf(scbEasyAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(kPlusAppEventUniqueID!=0){
            kPlusAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(kPlusAppEndTime, kPlusAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                kPlusAppStartTime = "";
                kPlusAppEndTime = "";
                kPlusAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "kPlusAppEventUniqueID", String.valueOf(kPlusAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(bualuangmBankingAppEventUniqueID!=0){
            bualuangmBankingAppEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(bualuangmBankingAppEndTime, bualuangmBankingAppEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                bualuangmBankingAppStartTime = "";
                bualuangmBankingAppEndTime = "";
                bualuangmBankingAppEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "bualuangmBankingAppEventUniqueID", String.valueOf(bualuangmBankingAppEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(lineCallEventUniqueID!=0){
            lineCallEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineCallEndTime, lineCallEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                lineCallStartTime = "";
                lineCallEndTime = "";
                lineCallEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "lineCallEventUniqueID", String.valueOf(lineCallEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(phoneCallEventUniqueID!=0){
            phoneCallEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneCallEndTime, phoneCallEventUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                phoneCallStartTime = "";
                phoneCallEndTime = "";
                phoneCallEventUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "phoneCallEventUniqueID", String.valueOf(phoneCallEventUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(houseBeaconUniqueID!=0){
            houseBeaconEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updateBeaconData(houseBeaconEndTime, houseBeaconUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                houseBeaconStartTime = "";
                houseBeaconEndTime = "";
                houseBeaconUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "houseBeaconUniqueID", String.valueOf(houseBeaconUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }
        if(hospitalBeaconUniqueID!=0){
            hospitalBeaconEndTime = dateFormat.format(date);
            Integer numberOfRowsUpdated = db.updateBeaconData(hospitalBeaconEndTime, hospitalBeaconUniqueID, stoppedStatus, serviceStopReason);
            if(numberOfRowsUpdated > 0){
                hospitalBeaconStartTime = "";
                hospitalBeaconEndTime = "";
                hospitalBeaconUniqueID = 0;
                updateSharedPreferencesValue(getApplicationContext(), "hospitalBeaconUniqueID", String.valueOf(hospitalBeaconUniqueID));
                Toast.makeText(getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
            }
        }

        updateSharedPreferencesValue(getApplicationContext(), "serviceStopReason", "-");
    }

    private void displayNotification(String title, String notificationMessage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("IO.SIS", "IO.SIS", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "IO.SIS")
                .setContentTitle(title)
                .setContentText(notificationMessage)
                .setSmallIcon(R.mipmap.ic_app_logo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationMessage));
        notificationManager.notify(getUniqueNumber(), notification.build());
    }

    public void getScreenStatus(){
        pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);

        String screenStatusValue = pref.getString("screenStatus", null);
        if(screenStatusValue == "screenOn"){
            isScreenOn = true;
            Log.e(applicationName, "Screen on");
        }
        else if(screenStatusValue == "screenOff"){
            isScreenOn = false;
            Log.e(applicationName, "Screen Off");
        }
    }

    /*public String retrieveNewApp() {
        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = null;
            //long appUsageDuration = 0;
            UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    System.out.println(usageStats.getPackageName() + ": " + usageStats.getLastTimeUsed());
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    //appUsageDuration = mySortedMap.get(mySortedMap.lastKey()).getTotalTimeInForeground();
                }
            }
            //Log.e(applicationName, "Current App in foreground is: " + currentApp);
            //Toast.makeText(getApplicationContext(),"Current App in foreground is: " + currentApp, Toast.LENGTH_LONG).show();
            return currentApp;

        }
        else {

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm=(manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            //Log.e(applicationName, "Current App in foreground is: " + mm);
            //Toast.makeText(getApplicationContext(),"Current App in foreground is: " + mm, Toast.LENGTH_LONG).show();
            return mm;
        }
    }
    */

    private String getForegroundActivity() {
        String packageName = "-";
        String className = "-";

        if(hasUsageStatsPermission(this.getApplicationContext())) {
            Calendar cal_begin = Calendar.getInstance();
            cal_begin.set(Calendar.YEAR, -1);
            long _begTime = cal_begin.getTimeInMillis();
            long _endTime = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager != null) {
                UsageEvents queryEvents = usageStatsManager.queryEvents(_begTime, _endTime);
                if (queryEvents != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (queryEvents.hasNextEvent()) {
                        UsageEvents.Event eventAux = new UsageEvents.Event();
                        queryEvents.getNextEvent(eventAux);
                        if (eventAux.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            event = eventAux;
                        }
                    }
                    packageName = event.getPackageName();
                    Log.d("Opened", packageName);
                    className = event.getClassName();
                    Log.d("Opened_EVENT", className);
                }
            }
        }
        else{
            stopServiceAsRequirementsNotMet("Usage Stats Permission Revoked");
        }
        return packageName;
    }

    private String getBackgroundActivity() {
        String packageName = "-";
        String className = "-";
        if(hasUsageStatsPermission(this.getApplicationContext())) {
            Calendar cal_begin = Calendar.getInstance();
            cal_begin.set(Calendar.YEAR, -1);
            long _begTime = cal_begin.getTimeInMillis();
            long _endTime = System.currentTimeMillis();
            UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usageStatsManager != null) {
                UsageEvents queryEvents = usageStatsManager.queryEvents(_begTime, _endTime);
                if (queryEvents != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    while (queryEvents.hasNextEvent()) {
                        UsageEvents.Event eventAux = new UsageEvents.Event();
                        queryEvents.getNextEvent(eventAux);
                        if (eventAux.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND){
                            event = eventAux;
                        }
                    }
                    packageName = event.getPackageName();
                    Log.d("Closed", packageName);
                    className = event.getClassName();
                    Log.d("Closed_EVENT", className);
                }
            }
        }
        else{
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            stopService(serviceIntent);
        }
        return packageName;
    }

    public void updateSharedPreferencesValue(Context context, String key, String value){
        pref = context.getApplicationContext().getSharedPreferences("IO.SIS", 0);
        editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean isLineCallInProgress(Context context){
        boolean isCallInProgress;
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int mode = am.getMode();
        if(AudioManager.MODE_IN_COMMUNICATION == mode){
            isCallInProgress = true;
            Log.e(applicationName, "VoIP CALL IS RUNNING");
        }
        else{
            isCallInProgress = false;
        }
        return isCallInProgress;
    }

    public boolean isPhoneCallInProgress(Context context){
        boolean phoneCallInProgress;
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int mode = am.getMode();
        if(AudioManager.MODE_IN_CALL == mode){
            phoneCallInProgress = true;
            Log.e(applicationName, "Phone CALL IS RUNNING");
        }
        else{
            phoneCallInProgress = false;
        }
        return  phoneCallInProgress;
    }

    public boolean isPhoneCharging(Context context) {
        boolean isCharging;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
            isCharging = true;
            Log.e(applicationName, "Phone is charging");
        }
        else{
            isCharging = false;
        }
        return isCharging;
    }

    public int getUniqueNumber(){
        Random rndNumber = new Random();
        // random number from 0 to 1000
        int randomNumber = rndNumber.nextInt(1000);
        int uniqueNumber = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE) + randomNumber;
        return uniqueNumber;
    }

    boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    public boolean hasCoarseLocationPermission(){
        boolean coarseLocationResult = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // not granted
                coarseLocationResult = false;
            }
            else{
                // granted
                coarseLocationResult = true;
            }
        }
        Log.e(applicationName, "Coarse Location Permission Result: " + coarseLocationResult);
        return coarseLocationResult;
    }

    public boolean hasLocationServiceEnabled(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopServiceAsRequirementsNotMet(String stopReason){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String notificationMessage = stopReason + " on " + dateFormat.format(date) + ", please enter the application to enable permissions.";
        displayNotification(applicationName, notificationMessage);
        updateSharedPreferencesValue(getApplicationContext(), "serviceStopReason", stopReason);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconManager.removeAllRangeNotifiers();
        mBeaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                if (beacons.size() > 0) {
                    // TO check the number of detected beacons:
                    Log.e(applicationName, "Number of beacons detected: " + String.valueOf(beacons.size()));
                    // Loop through each beacon detected
                    for(Beacon detectedBeacon : beacons) {
                        if(Identifier.parse(detectedBeacon.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(detectedBeacon.getId2().toString()).equals(Identifier.parse("0xabcde0ab00c6"))){
                            if(!houseBeaconDetailsRecorded){
                                // Check for telemetry data
                                if (detectedBeacon.getExtraDataFields().size() > 0) {
                                    long telemetryVersion = detectedBeacon.getExtraDataFields().get(0);
                                    long batteryMilliVolts = detectedBeacon.getExtraDataFields().get(1);
                                    long unsignedTemp = (detectedBeacon.getExtraDataFields().get(2) >> 8);
                                    double temperature = unsignedTemp > 128 ? unsignedTemp - 256 : unsignedTemp + (detectedBeacon.getExtraDataFields().get(2) & 0xff) / 256.0;
                                    long pduCount = detectedBeacon.getExtraDataFields().get(3);
                                    long uptime = detectedBeacon.getExtraDataFields().get(4);

                                    displayNotification(applicationName, dateFormat.format(date) + " @@@ Entered the white (HOUSE) beacon region that is transmitting namespace id: " + detectedBeacon.getId1() +
                                            " and instance id: " + detectedBeacon.getId2() +
                                            " approximately " + detectedBeacon.getDistance() + " meters away.\n--------------------- \nThe above beacon is sending telemetry version " + telemetryVersion +
                                            ", has been up for : " + uptime + " seconds" +
                                            ", has a battery level of " + batteryMilliVolts + " mV" +
                                            ", and has transmitted " + pduCount + " advertisements.\n It has a temperature of " + temperature + "°C\n\n=====================================");

                                    houseBeaconStartTime = dateFormat.format(date);
                                    houseBeaconUniqueID = getUniqueNumber();
                                    boolean isInserted = db.insertBeaconData(Integer.parseInt(participantPin), String.valueOf(detectedBeacon.getId1()), String.valueOf(detectedBeacon.getId2()), "House", houseBeaconStartTime, "-", houseBeaconUniqueID, String.valueOf(batteryMilliVolts), String.valueOf(temperature), runningStatus, "-");
                                    if(isInserted){
                                        updateSharedPreferencesValue(getApplicationContext(), "houseBeaconUniqueID", String.valueOf(houseBeaconUniqueID));
                                        houseBeaconDetailsRecorded = true;
                                    }

                                    try {
                                        mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHouseId, null, null, null));
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        if (Identifier.parse(detectedBeacon.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(detectedBeacon.getId2().toString()).equals(Identifier.parse("0xabcde0a20030"))) {
                            if(!hospitalBeaconDetailsRecorded){
                                // Check for telemetry data
                                if (detectedBeacon.getExtraDataFields().size() > 0) {
                                    long telemetryVersion = detectedBeacon.getExtraDataFields().get(0);
                                    long batteryMilliVolts = detectedBeacon.getExtraDataFields().get(1);
                                    long unsignedTemp = (detectedBeacon.getExtraDataFields().get(2) >> 8);
                                    double temperature = unsignedTemp > 128 ? unsignedTemp - 256 : unsignedTemp + (detectedBeacon.getExtraDataFields().get(2) & 0xff) / 256.0;
                                    long pduCount = detectedBeacon.getExtraDataFields().get(3);
                                    long uptime = detectedBeacon.getExtraDataFields().get(4);

                                    displayNotification(applicationName, dateFormat.format(date) + " @@@ Entered the black (HOSPITAL) beacon region that is transmitting namespace id: " + detectedBeacon.getId1() +
                                            " and instance id: " + detectedBeacon.getId2() +
                                            " approximately " + detectedBeacon.getDistance() + " meters away.\n---------------------\nThe above beacon is sending telemetry version " + telemetryVersion +
                                            ", has been up for : " + uptime + " seconds" +
                                            ", has a battery level of " + batteryMilliVolts + " mV" +
                                            ", and has transmitted " + pduCount + " advertisements.\n It has a temperature of " + temperature + "°C\n\n=====================================");

                                    hospitalBeaconStartTime = dateFormat.format(date);
                                    hospitalBeaconUniqueID = getUniqueNumber();
                                    boolean isInserted = db.insertBeaconData(Integer.parseInt(participantPin), String.valueOf(detectedBeacon.getId1()), String.valueOf(detectedBeacon.getId2()), "Hospital", hospitalBeaconStartTime, "-", hospitalBeaconUniqueID, String.valueOf(batteryMilliVolts), String.valueOf(temperature), runningStatus, "-");
                                    if(isInserted){
                                        updateSharedPreferencesValue(getApplicationContext(), "hospitalBeaconUniqueID", String.valueOf(hospitalBeaconUniqueID));
                                        hospitalBeaconDetailsRecorded = true;
                                    }

                                    try {
                                        mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHospitalId, null, null, null));
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                    }
                }
            }
        });

        mBeaconManager.removeAllMonitorNotifiers();
        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {

            }

            @Override
            public void didExitRegion(Region region) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                if(Identifier.parse(region.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(region.getId2().toString()).equals(Identifier.parse("0xabcde0a20030"))){
                    Log.e(applicationName, "@@@ LEFT the black beacon(HOSPITAL) in the region with namespace id " + region.getId1() + " and instance id: " + region.getId2());
                    displayNotification(applicationName, dateFormat.format(date) +"@@@ LEFT the black beacon(HOSPITAL) in the region with namespace id " + region.getId1() + " and instance id: " + region.getId2() +".");

                    hospitalBeaconEndTime = dateFormat.format(date);
                    Integer numberOfRowsUpdated = db.updateBeaconData(hospitalBeaconEndTime, hospitalBeaconUniqueID, stoppedStatus, "-");
                    if(numberOfRowsUpdated > 0){
                        hospitalBeaconStartTime = "";
                        hospitalBeaconEndTime = "";
                        hospitalBeaconUniqueID = 0;
                        updateSharedPreferencesValue(getApplicationContext(), "hospitalBeaconUniqueID", String.valueOf(hospitalBeaconUniqueID));
                    }


                    try {
                        mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHospitalId, null, null, null));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    hospitalBeaconDetailsRecorded = false;
                }
                if(Identifier.parse(region.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(region.getId2().toString()).equals(Identifier.parse("0xabcde0ab00c6"))){
                    Log.e(applicationName, "@@@ LEFT the white beacon(HOUSE) in the region with namespace id " + region.getId1() + " and instance id: " + region.getId2());
                    displayNotification(applicationName, dateFormat.format(date) +"@@@ LEFT the white beacon(HOUSE) in the region with namespace id " + region.getId1() + " and instance id: " + region.getId2() +".");

                    houseBeaconEndTime = dateFormat.format(date);
                    Integer numberOfRowsUpdated = db.updateBeaconData(houseBeaconEndTime, houseBeaconUniqueID, stoppedStatus, "-");
                    if(numberOfRowsUpdated > 0){
                        houseBeaconStartTime = "";
                        houseBeaconEndTime = "";
                        houseBeaconUniqueID = 0;
                        updateSharedPreferencesValue(getApplicationContext(), "houseBeaconUniqueID", String.valueOf(houseBeaconUniqueID));
                    }
                    try {
                        mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHouseId, null, null, null));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    houseBeaconDetailsRecorded = false;
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                if(state == 1){
                    if (Identifier.parse(region.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(region.getId2().toString()).equals(Identifier.parse("0xabcde0a20030"))) {
                        Log.e(applicationName, dateFormat.format(date) +" @@@ ENTERED the black beacon(HOSPITAL) region with namespace id " + region.getId1() + " and instance id: " + region.getId2() + "\n\n\n");
                        if(!hospitalBeaconDetailsRecorded){
                            // if in range, start getting beacon details
                            try {
                                mBeaconManager.startRangingBeaconsInRegion(new Region(rangingHospitalId, null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHospitalId, null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(Identifier.parse(region.getId1().toString()).equals(Identifier.parse("0xc8251d848e225e4d6952")) && Identifier.parse(region.getId2().toString()).equals(Identifier.parse("0xabcde0ab00c6"))){
                        if(!houseBeaconDetailsRecorded){
                            //if in range, start getting beacon details
                            try {
                                mBeaconManager.startRangingBeaconsInRegion(new Region(rangingHouseId, null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            try {
                                mBeaconManager.stopRangingBeaconsInRegion(new Region(rangingHouseId, null, null, null));
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else{
                    // state = 0, not in any region / not in any beacon's region
                }
            }
        });

        try {
            mBeaconManager.startMonitoringBeaconsInRegion(new Region(monitoringHospitalId, Identifier.parse("0xc8251d848e225e4d6952"), Identifier.parse("0xabcde0a20030"), null));
            mBeaconManager.startMonitoringBeaconsInRegion(new Region(monitoringHouseId, Identifier.parse("0xc8251d848e225e4d6952"), Identifier.parse("0xabcde0ab00c6"), null));
        } catch (RemoteException e) {

        }

    }

}
