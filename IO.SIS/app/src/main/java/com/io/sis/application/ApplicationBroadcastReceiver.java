package com.io.sis.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ApplicationBroadcastReceiver extends BroadcastReceiver {

    String applicationName = "IO.SIS";

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("IO.SIS", 0);
        /*** Detect Phone Shutdown ***/
        if (intent.getAction().equals(Intent.ACTION_SHUTDOWN )) {
            String serviceEventUniqueID = pref.getString("serviceEventUniqueID", "0");
            String screenEventUniqueID = pref.getString("screenEventUniqueID", "0");
            String phoneChargeEventUniqueID = pref.getString("phoneChargeEventUniqueID", "0");
            String facebookAppEventUniqueID = pref.getString("facebookAppEventUniqueID", "0");
            String youtubeAppEventUniqueID = pref.getString("youtubeAppEventUniqueID", "0");
            String lineAppEventUniqueID = pref.getString("lineAppEventUniqueID", "0");
            String scbEasyAppEventUniqueID = pref.getString("scbEasyAppEventUniqueID", "0");
            String kPlusAppEventUniqueID = pref.getString("kPlusAppEventUniqueID", "0");
            String bualuangmBankingAppEventUniqueID = pref.getString("bualuangmBankingAppEventUniqueID", "0");
            String lineCallEventUniqueID = pref.getString("lineCallEventUniqueID", "0");
            String phoneCallEventUniqueID = pref.getString("phoneCallEventUniqueID", "0");
            String hospitalBeaconUniqueID = pref.getString("hospitalBeaconUniqueID", "0");
            String houseBeaconUniqueID = pref.getString("houseBeaconUniqueID", "0");

            DatabaseHelper db = new DatabaseHelper(context);
            String serviceStopReason = "Phone Turned Off";

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            if(!serviceEventUniqueID.equals("0")){
                String serviceEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(serviceEndTime, Integer.valueOf(serviceEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "serviceEventUniqueID", "0");
                    updateSharedPreferencesValue(context.getApplicationContext(), "serviceStopReason", "-");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!screenEventUniqueID.equals("0")){
                String screenEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(screenEndTime, Integer.valueOf(screenEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "screenEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!phoneChargeEventUniqueID.equals("0")){
                String phoneChargeEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneChargeEndTime, Integer.valueOf(phoneChargeEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "phoneChargeEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!facebookAppEventUniqueID.equals("0")){
                String facebookAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(facebookAppEndTime, Integer.valueOf(facebookAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "facebookAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!youtubeAppEventUniqueID.equals("0")){
                String youtubeAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(youtubeAppEndTime, Integer.valueOf(youtubeAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "youtubeAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!lineAppEventUniqueID.equals("0")){
                String lineAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineAppEndTime, Integer.valueOf(lineAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "lineAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!scbEasyAppEventUniqueID.equals("0")){
                String scbEasyAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(scbEasyAppEndTime, Integer.valueOf(scbEasyAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "scbEasyAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!kPlusAppEventUniqueID.equals("0")){
                String kPlusAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(kPlusAppEndTime, Integer.valueOf(kPlusAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "kPlusAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!bualuangmBankingAppEventUniqueID.equals("0")){
                String bualuangmBankingAppEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(bualuangmBankingAppEndTime, Integer.valueOf(bualuangmBankingAppEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "bualuangmBankingAppEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!lineCallEventUniqueID.equals("0")){
                String lineCallEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(lineCallEndTime, Integer.valueOf(lineCallEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "lineCallEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!phoneCallEventUniqueID.equals("0")){
                String phoneCallEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updatePhoneUsageData(phoneCallEndTime, Integer.valueOf(phoneCallEventUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "phoneCallEventUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!hospitalBeaconUniqueID.equals("0")){
                String hospitalBeaconEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updateBeaconData(hospitalBeaconEndTime, Integer.valueOf(hospitalBeaconUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "hospitalBeaconUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
            if(!houseBeaconUniqueID.equals("0")){
                String houseBeaconEndTime = dateFormat.format(date);
                Integer numberOfRowsUpdated = db.updateBeaconData(houseBeaconEndTime, Integer.valueOf(houseBeaconUniqueID), "Stopped", serviceStopReason);
                if(numberOfRowsUpdated > 0){
                    updateSharedPreferencesValue(context.getApplicationContext(), "houseBeaconUniqueID", "0");
                    Toast.makeText(context.getApplicationContext(), numberOfRowsUpdated + " Data Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
        /*** Detect Phone Setup Complete ***/
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            String serviceRunningStatus = pref.getString("serviceRunningStatus", "serviceStopped");
            String participantPin = pref.getString("participantPin", "0");
            //Toast.makeText(context, serviceRunningStatus ,Toast.LENGTH_SHORT).show();
            //Toast.makeText(context, participantPin ,Toast.LENGTH_SHORT).show();
            if(serviceRunningStatus.equals("serviceRunning") && Integer.valueOf(participantPin) != 0){
                Intent serviceIntent = new Intent(context, ForegroundService.class);
                //serviceIntent.putExtra("inputExtra", input);
                /*** startForegroundService only available on API level 26 but handled by ContextCompat
                 Select startForegroundService and Ctrl + B to read its if else conditions
                 Same as:
                 if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                 }
                 else {
                    context.startService(serviceIntent);
                 }
                 ***/
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
        /*** Detect Phone Screen On/Off ***/
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            updateSharedPreferencesValue(context, "screenStatus", "screenOff");
            //Toast.makeText(context, "Screen Off",Toast.LENGTH_SHORT).show();
            Log.e(applicationName, "Screen Off");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            updateSharedPreferencesValue(context, "screenStatus", "screenOn");
            //Toast.makeText(context, "Screen On",Toast.LENGTH_SHORT).show();
            Log.e(applicationName, "Screen On");
        }
    }

    public void updateSharedPreferencesValue(Context context, String key, String value){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("IO.SIS", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
