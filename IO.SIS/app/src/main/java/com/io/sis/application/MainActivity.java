package com.io.sis.application;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.altbeacon.beacon.BeaconManager;
public class MainActivity extends AppCompatActivity {
    //private EditText editTextInput;
    String applicationName = "IO.SIS";
    int uniqueUsageIntentId = 123;
    int uniqueLocationIntentId = 456;
    View startServiceButton, stopServiceButton, grantPermissionButton, statusRelativeLayout;
    TextView permissionDescription, statusText;
    ImageView statusImage;
    boolean isBluetoothEnabled = false;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    LocationManager locationManager ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //editTextInput = findViewById(R.id.edit_text_input);
        //editTextInput.setText("HI");
        startServiceButton = findViewById(R.id.start_service);
        stopServiceButton = findViewById(R.id.stop_service);
        grantPermissionButton = findViewById(R.id.grant_permission);
        permissionDescription = findViewById(R.id.permissionDescription);
        statusRelativeLayout = findViewById(R.id.statusRelativeLayout);
        statusImage = findViewById(R.id.statusImage);
        statusText = findViewById(R.id.statusText);
        //requestUsageStatsPermission();
        hasBluetoothEnabled();
        buttonDisplayLogic();


        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void startService(View v) {
        //String input = editTextInput.getText().toString();
        startServiceButton.setVisibility(View.GONE);
        stopServiceButton.setVisibility(View.VISIBLE);
        grantPermissionButton.setVisibility(View.GONE);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        //serviceIntent.putExtra("inputExtra", input);

        // Only available on API level 26 but handled by ContextCompat
        // Select startForegroundService and Ctrl + B to read its if else conditions
        ContextCompat.startForegroundService(this, serviceIntent);
        statusRelativeLayout.setVisibility(View.VISIBLE);
        statusImage.setImageResource(R.drawable.ic_green_icon);
        statusText.setText("Service Running");
    }

    public void stopService(View v) {
        startServiceButton.setVisibility(View.VISIBLE);
        stopServiceButton.setVisibility(View.GONE);
        grantPermissionButton.setVisibility(View.GONE);
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        statusRelativeLayout.setVisibility(View.VISIBLE);
        statusImage.setImageResource(R.drawable.ic_red_icon);
        statusText.setText("Service Stopped");

        SharedPreferences pref;
        SharedPreferences.Editor editor;
        pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
        editor = pref.edit();
        editor.putString("serviceStopReason", "Stop Button Pressed");
        editor.apply();
    }

    public void grantPermission (View v){
        displayPermissionDialog();
    }


//    void requestUsageStatsPermission() {
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !hasUsageStatsPermission(this)) {
//            //displayPermissionDialog();
//        }
//        else{
//            buttonDisplayLogic();
//        }
//    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }

    public void hasBluetoothEnabled(){
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                // Bluetooth not enabled
                isBluetoothEnabled = false;
            }
            else{
                // Bluetooth enabled
                isBluetoothEnabled = true;
            }
        }
        catch (RuntimeException e) {
            // Bluetooth LE not supported
            isBluetoothEnabled = false;
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("BLE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton("Continue", null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }
            });
            builder.show();
        }
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
        return coarseLocationResult;
    }

    public boolean hasLocationServiceEnabled(){
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void buttonDisplayLogic(){
        // Usage stats permission granted and bluetooth granted and coarse location granted and location enabled
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasUsageStatsPermission(this) && isBluetoothEnabled && hasCoarseLocationPermission() && hasLocationServiceEnabled()) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
            String isServiceRunning = pref.getString("serviceRunningStatus", "serviceStopped");
            Log.e(applicationName, isServiceRunning);

            if(isServiceRunning.equals("serviceStopped")){
                startServiceButton.setVisibility(View.VISIBLE);
                stopServiceButton.setVisibility(View.GONE);
                grantPermissionButton.setVisibility(View.GONE);
                permissionDescription.setVisibility(View.GONE);
                statusRelativeLayout.setVisibility(View.VISIBLE);
                statusImage.setImageResource(R.drawable.ic_red_icon);
                statusText.setText("Service Stopped");
            }
            else if(isServiceRunning.equals("serviceRunning")){
                startServiceButton.setVisibility(View.GONE);
                stopServiceButton.setVisibility(View.VISIBLE);
                grantPermissionButton.setVisibility(View.GONE);
                permissionDescription.setVisibility(View.GONE);
                statusRelativeLayout.setVisibility(View.VISIBLE);
                statusImage.setImageResource(R.drawable.ic_green_icon);
                statusText.setText("Service Running");
            }
        }
        // Usage stats permission not granted or bluetooth not granted or coarse location not granted or location not on
        else{

        }

    }

    public void displayPermissionDialog(){
        // usage stats permission not granted
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !hasUsageStatsPermission(this)) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Permission Request");
            builder1.setIcon(R.mipmap.team_logo);
            builder1.setMessage("IO.SIS requires access to analyze your phone usage. \n\nTo grant access, select 'Continue' > 'IO.SIS' App > 'Allow Usage Tracking'.");
            builder1.setCancelable(false);
            builder1.setPositiveButton(
                    "Continue",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //dialog.cancel();
                            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),uniqueUsageIntentId);
                        }
                    });
//            builder1.setNegativeButton(
//                    "Cancel",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.cancel();
//                            Log.e(applicationName, "CANCELLED AND NOT GRANTED");
//                            //grantPermissionButton.setVisibility(View.VISIBLE);
//                            displayPermissionDialog();
//                        }
//                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        // bluetooth not enabled
        else if(!isBluetoothEnabled){
            displayBluetoothPermissionDialog();
        }
        else if(!hasCoarseLocationPermission()){
            displayCoarseLocationPermissionDialog();
        }
        else if(!hasLocationServiceEnabled()){
            displayLocationPermissionDialog();
        }

        // granted
        else{
            buttonDisplayLogic();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Check which request we're responding to
        if (requestCode == uniqueUsageIntentId) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user pressed ok
            }else{
                // The user pressed cancel
                //requestUsageStatsPermission();
                //Toast.makeText(getApplicationContext(), "Exit from settings", Toast.LENGTH_SHORT).show();
                //Check if permission granted
                if(hasUsageStatsPermission(getApplicationContext())){
                    Log.e(applicationName, "EXIT AND GRANTED");
                    displayPermissionDialog();
                }
                else{
                    Log.e(applicationName, "EXIT AND NOT GRANTED");
                    grantPermissionButton.setVisibility(View.VISIBLE);
                    displayPermissionDialog();
                }
            }
        }
        if (requestCode == uniqueLocationIntentId) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user pressed ok
            }else{
                // The user pressed cancel
                //requestUsageStatsPermission();
                //Toast.makeText(getApplicationContext(), "Exit from settings", Toast.LENGTH_SHORT).show();
                //Check if permission granted
                if(hasLocationServiceEnabled()){
                    Log.e(applicationName, "Location Service EXIT AND GRANTED");
                    displayPermissionDialog();
                }
                else{
                    Log.e(applicationName, "Location Service EXIT AND NOT GRANTED");
                    grantPermissionButton.setVisibility(View.VISIBLE);
                    displayPermissionDialog();
                }
            }
        }
    }

    public void displayBluetoothPermissionDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth Required");
        builder.setIcon(R.mipmap.team_logo);
        builder.setCancelable(false);
        builder.setMessage(Html.fromHtml("IO.SIS requires bluetooth to be enabled whenever the service is running and <b>will automatically turn the bluetooth on</b> if it is turned off.<br><br>Please enable your bluetooth."));
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                boolean isEnabled = bluetoothAdapter.isEnabled();
                if (!isEnabled) {
                    bluetoothAdapter.enable();
                    isBluetoothEnabled = true;
                    displayPermissionDialog();
                }
            }
        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                displayPermissionDialog();
//            }
//        });
        builder.show();
    }

    public void displayCoarseLocationPermissionDialog(){

        /***
         Apps that target SDK 23+ (Android Marshmallow) must also prompt the user for a location permission after the app is launched.
         If you fail to prompt for and get this permission on apps on apps targeting SDK 23+, you won’t detect beacons in either the
         background or the foreground.

         For apps that target SDKS earlier than 23, failing to include the coarse location permission in the manifest will cause Android 6
         devices to fail to scan in the background.
         ***/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // not granted
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Permission Request");
                builder.setIcon(R.mipmap.team_logo);
                builder.setCancelable(false);
                builder.setMessage("IO.SIS requires location access to detect beacons.");
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        displayPermissionDialog();
//                    }
//                });
//                builder.setPositiveButton("Continue", null);
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                                PERMISSION_REQUEST_COARSE_LOCATION);
//                    }
//                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    displayPermissionDialog();
                } else {
                    // not granted
                    displayPermissionDialog();
                }
                return;
            }
        }
    }

    public void displayLocationPermissionDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Request");
        builder.setIcon(R.mipmap.team_logo);
        builder.setCancelable(false);
        builder.setMessage("IO.SIS requires your location service to be turned on.");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent locationServiceIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(locationServiceIntent, uniqueLocationIntentId);
            }
        });
        builder.show();
    }





















    public void clearPin(View v){
        SharedPreferences preferences = getSharedPreferences("IO.SIS", 0);
        if(preferences.contains("participantPin")) {
            preferences.edit().remove("participantPin").apply();
            Toast.makeText(getApplicationContext(), "Pin cleared", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Already cleared pin", Toast.LENGTH_LONG).show();
        }
    }

    public void viewAllUsageRecords(View v){
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor result = db.getAllPhoneUsageData();
        if(result.getCount() == 0) {
            // show message
            showMessage("Usage Records","No records found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (result.moveToNext()) {
            buffer.append("ID: "+ result.getInt(0)+"\n");
            buffer.append("UID: "+ result.getInt(1)+"\n");
            buffer.append("COMPONENT: "+ result.getString(2)+"\n");
            buffer.append("START_TIME: "+ result.getString(3)+"\n");
            buffer.append("END_TIME: "+ result.getString(4)+"\n");
            buffer.append("EVENT_ID: "+ result.getInt(5)+"\n");
            buffer.append("STATUS: "+ result.getString(6)+"\n");
            buffer.append("COMMENT: "+ result.getString(7)+"\n\n");
        }

        // Show all data
        showMessage("Usage Records",buffer.toString());
    }

    public void deleteAllUsageRecords(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
        String participantPin = pref.getString("participantPin", "0");

        DatabaseHelper db = new DatabaseHelper(this);
        Integer deletedRows = db.deleteAllPhoneUsageData(participantPin);
        if(deletedRows > 0) {
            Toast.makeText(getApplicationContext(), "Usage Deleted rows: " + deletedRows, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this,"Usage Data not Deleted",Toast.LENGTH_LONG).show();
        }
    }

    public void viewAllBeaconRecords(View v){
        DatabaseHelper db = new DatabaseHelper(this);
        Cursor result = db.getAllBeaconData();
        if(result.getCount() == 0) {
            // show message
            showMessage("Beacon Records","No records found");
            return;
        }

        StringBuffer buffer = new StringBuffer();
        while (result.moveToNext()) {
            buffer.append("ID: "+ result.getInt(0)+"\n");
            buffer.append("UID: "+ result.getInt(1)+"\n");
            buffer.append("NAMESPACE_ID: "+ result.getString(2)+"\n");
            buffer.append("INSTANCE_ID: "+ result.getString(3)+"\n");
            buffer.append("BEACON_TYPE: "+ result.getString(4)+"\n");
            buffer.append("START_TIME: "+ result.getString(5)+"\n");
            buffer.append("END_TIME: "+ result.getString(6)+"\n");
            buffer.append("EVENT_ID: "+ result.getInt(7)+"\n");
            buffer.append("BATTERY: "+ result.getString(8)+"\n");
            buffer.append("TEMPERATURE: "+ result.getString(9)+"\n");
            buffer.append("STATUS: "+ result.getString(10)+"\n");
            buffer.append("COMMENT: "+ result.getString(11)+"\n\n");
        }

        // Show all data
        showMessage("Beacon Records",buffer.toString());
    }

    public void deleteAllBeaconRecords(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("IO.SIS", 0);
        String participantPin = pref.getString("participantPin", "0");

        DatabaseHelper db = new DatabaseHelper(this);
        Integer deletedRows = db.deleteAllBeaconData(participantPin);
        if(deletedRows > 0) {
            Toast.makeText(getApplicationContext(), "Beacon deleted rows: " + deletedRows, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this,"Beacon Data not Deleted",Toast.LENGTH_LONG).show();
        }
    }

    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }
}


