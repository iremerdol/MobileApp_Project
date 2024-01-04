package com.example.teamproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.net.Uri;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SharedPreferences sharedPreferences;
    private boolean isPermissionGranted = false;
    private Snackbar permissionSnackbar;
    private static final String PREF_NAME = "StepCounterPrefs";
    private static final String STEP_COUNT_KEY = "stepCount";
    private static final String MIDNIGHT_KEY = "midnight";
    private static final String IS_REDEEMED = "isredeemed";

    private ProgressBar progressBar;
    private TextView steps, textBalance;
    private Button buttonRedeem;
    private ImageButton buttonLogOut, buttonShare, buttonWallet, buttonArticle;
    private FirebaseAuth mAuth;
    int firstStepCount;
    boolean firstFlag = false, isRedeemed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonShare = findViewById(R.id.buttonShare);
        buttonWallet = findViewById(R.id.buttonWallet);
        textBalance = findViewById(R.id.textBalance);
        buttonRedeem = findViewById(R.id.buttonRedeem);
        progressBar = findViewById(R.id.progressBar);
        buttonArticle = findViewById(R.id.buttonArticle);
        steps = findViewById(R.id.steps);

        if(checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED){
            isPermissionGranted = true;
            enableUI();
        }
        else{
            permissionSnackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Please grant permission for physical activity.",
                    Snackbar.LENGTH_INDEFINITE);
            permissionSnackbar.setAction("Grant", v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });
            permissionSnackbar.show();
            disableUI();
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(mAuth.getUid().toString())
                .get().addOnCompleteListener(
                        task -> {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                Long balance = document.getLong("balance");
                                textBalance.setText(balance+" STY");
                            }
                            else{
                                textBalance.setText("Err");
                            }
                        }
                );

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isRedeemed = sharedPreferences.getBoolean(IS_REDEEMED,false);
        resetStepCountIfMidnight();

        if(isRedeemed){
            buttonRedeem.setVisibility(View.INVISIBLE);
            buttonRedeem.setClickable(false);
            buttonRedeem.setBackgroundColor(Color.parseColor("#F1BD9B")); // #ed7525
        }
        else{
            buttonRedeem.setClickable(false);
            buttonRedeem.setBackgroundColor(Color.parseColor("#F1BD9B")); // #ed7525
        }
        firstFlag = true;

        // Register the sensor listener
        if(stepCounterSensor != null){
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else{
            Toast.makeText(this, "Step Counter Sensor not available", Toast.LENGTH_SHORT).show();
        }

        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, contacts.class);
                startActivity(intent);
            }
        });

        buttonArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(MainActivity.this, ArticleAdapter.class);
                startService(serviceIntent);
            }
        });
        buttonWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, shop.class);
                startActivity(intent);
            }
        });

        buttonRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharedPreferences.getBoolean(IS_REDEEMED,true)){
                    buttonRedeem.setClickable(false);
                    buttonRedeem.setVisibility(View.INVISIBLE);
                    sharedPreferences.edit().putBoolean(IS_REDEEMED,true).apply();
                    isRedeemed = true;
                }
                else{
                    Toast.makeText(MainActivity.this, "You got 137 STY yayyyyy!!!", Toast.LENGTH_SHORT).show();
                    buttonRedeem.setClickable(false);
                    buttonRedeem.setVisibility(View.INVISIBLE);
                    sharedPreferences.edit().putBoolean(IS_REDEEMED,true).apply();
                    isRedeemed = true;
                    db.collection("users").document(mAuth.getUid().toString())
                            .get().addOnCompleteListener(
                                    task -> {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", document.getString("email"));
                                            user.put("pass", document.getString("pass"));
                                            user.put("balance", document.getLong("balance") + 137);
                                            user.put("gifts", document.getLong("gifts"));
                                            db.collection("users").document(mAuth.getUid().toString()).set(user);
                                            textBalance.setText(document.getLong("balance") + 137 +" STY");
                                        }
                                    });
                }
            }
        });

        buttonRedeem.setClickable(false);
        steps.setText("Total Steps " + String.valueOf(0));
        progressBar.setProgress(0);

        if(!isPermissionGranted){
            requestPermission();
        }

        permissionSnackbar = Snackbar.make(findViewById(android.R.id.content),
                "Please grant permission for physical activity.",
                Snackbar.LENGTH_INDEFINITE);

        disableUI();
        permissionSnackbar.setAction("Grant", v -> requestPermission());
        permissionSnackbar.show();
    }
    @Override
    protected void onResume(){
        super.onResume();
        checkPermissionStatus();
        if(isRedeemed){
            buttonRedeem.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        checkPermissionStatus();
        if(isRedeemed){
            buttonRedeem.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(stepCounterSensor != null){
            sensorManager.unregisterListener(this);
        }
    }

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String jsonData = intent.getStringExtra("jsonData");

            // Parse the JSON data and display it (You can use a library like Gson for parsing)
            try {
                JSONArray jsonArray = new JSONArray(jsonData);

                // Display the first post title
                if (jsonArray.length() > 0) {
                    JSONObject firstPost = jsonArray.getJSONObject(0);
                    String title = firstPost.getString("title");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void requestPermission(){
        requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 101);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                isPermissionGranted = true;
                enableUI();
            }
            else{
                permissionSnackbar = Snackbar.make(findViewById(android.R.id.content),
                        "Please grant permission for physical activity.",
                        Snackbar.LENGTH_INDEFINITE);

                permissionSnackbar.setAction("Open Settings", v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                });
                permissionSnackbar.show();
                disableUI();
            }
        }
    }
    private void checkPermissionStatus(){
        if(checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED){
            isPermissionGranted = true;
            enableUI();
            if(permissionSnackbar != null && permissionSnackbar.isShown()){
                permissionSnackbar.dismiss();
            }
        }
        else{
            if(permissionSnackbar != null && !permissionSnackbar.isShown()){
                permissionSnackbar.show();
            }
            disableUI();
        }
    }
    private void disableUI(){
        buttonLogOut.setEnabled(false);
        buttonShare.setEnabled(false);
        buttonWallet.setEnabled(false);
        buttonRedeem.setEnabled(false);
    }
    private void enableUI(){
        buttonLogOut.setEnabled(true);
        buttonShare.setEnabled(true);
        buttonWallet.setEnabled(true);
        buttonRedeem.setEnabled(true);
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            // Retrieve the saved step count

            if(firstFlag == true){
                firstFlag = false;
                firstStepCount = (int) event.values[0];
            }

            int savedStepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);

            // Increment the step count
            int currentStepCount = (int) event.values[0];
            int newStepCount = (currentStepCount - firstStepCount);
            newStepCount /= 10;

            // Save the new step count
            sharedPreferences.edit().putInt(STEP_COUNT_KEY, savedStepCount + newStepCount).apply();

            savedStepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);

            isRedeemed = sharedPreferences.getBoolean(IS_REDEEMED,true);

            // Check if the step count exceeds 10k
            if(!isRedeemed && isPermissionGranted){
                if(savedStepCount >= 1){
                    buttonRedeem.setClickable(true);
                    buttonRedeem.setBackgroundColor(Color.parseColor("#ed7525"));
                    // Reset the step count
                    //sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).apply();
                }
            }
            steps.setText("Total Steps " + String.valueOf(savedStepCount));

            progressBar.setProgress(savedStepCount);
        }
    }
    private void resetStepCountIfMidnight(){
        //Get the last day
        long lastMidnight = sharedPreferences.getLong(MIDNIGHT_KEY, 0);

        //Current time
        long currentTime = System.currentTimeMillis();

        //Check if it's a new day
        if(currentTime > lastMidnight + 24 * 60 * 60 * 1000){
            //Reset step and update last midnight timestamp
            sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).putLong(MIDNIGHT_KEY, getTodayMidnight()).putBoolean(IS_REDEEMED,false).apply();
            buttonRedeem.setVisibility(View.VISIBLE);
            buttonRedeem.setClickable(false);
            buttonRedeem.setBackgroundColor(Color.parseColor("#F1BD9B"));
            isRedeemed = false;
        }
        else{
            int lastSavedStep = sharedPreferences.getInt(STEP_COUNT_KEY, 0);
            steps.setText("Total Steps " + lastSavedStep);
            progressBar.setProgress(lastSavedStep);
        }
    }
    @NonNull
    private long getTodayMidnight(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}