package com.example.teamproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "StepCounterPrefs";
    private static final String STEP_COUNT_KEY = "stepCount";
    private static final String MIDNIGHT_KEY = "midnight";
    private ProgressBar progressBar;
    private TextView steps, textBalance;

    private ImageButton buttonLogOut, buttonShare, buttonWallet;

    private FirebaseAuth mAuth;

    int firstStepCount;
    boolean firstFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonShare = findViewById(R.id.buttonShare);
        buttonWallet = findViewById(R.id.buttonWallet);
        textBalance = findViewById(R.id.textBalance);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(mAuth.getUid().toString())
                .get().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                Long balance = document.getLong("balance");
                                textBalance.setText(balance+" STY");
                            } else {
                                textBalance.setText("Err");
                            }
                        }
                );

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        resetStepCountIfMidnight();

        firstFlag = true;

        // Register the sensor listener
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Step Counter Sensor not available", Toast.LENGTH_SHORT).show();
        }

        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, contacts.class);
                startActivity(intent);
                finish();
            }
        });

        buttonWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, shop.class);
                startActivity(intent);
                finish();
            }
        });

        progressBar = findViewById(R.id.progressBar);
        steps = findViewById(R.id.steps);

        steps.setText("Total Steps " + String.valueOf(0));

        progressBar.setProgress(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the sensor listener to save resources when the activity is destroyed
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Retrieve the saved step count

            if(firstFlag == true){
                firstFlag = false;
                firstStepCount = (int) event.values[0];
                if(sharedPreferences.getInt(STEP_COUNT_KEY, 0) == 0){
                    sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).apply();
                }
            }

            int savedStepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);

            // Increment the step count
            int currentStepCount = (int) event.values[0];
            int newStepCount = (currentStepCount - firstStepCount);
            newStepCount /= 10;

            // Save the new step count
            sharedPreferences.edit().putInt(STEP_COUNT_KEY, savedStepCount + newStepCount).apply();

            savedStepCount = sharedPreferences.getInt(STEP_COUNT_KEY, 0);

            // Check if the step count exceeds 10k
            if (savedStepCount >= 10000) {
                Toast.makeText(this, "Yayyyyy! You've taken over 10,000 steps!", Toast.LENGTH_SHORT).show();
                // Reset the step count
                sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).apply();
            }

            steps.setText("Total Steps " + String.valueOf(savedStepCount));

            progressBar.setProgress(savedStepCount);
        }
    }

    private void resetStepCountIfMidnight() {
        // Retrieve last midnight timestamp
        long lastMidnight = sharedPreferences.getLong(MIDNIGHT_KEY, 0);

        // Get current time
        long currentTime = System.currentTimeMillis();

        // Check if it's a new day
        if (currentTime > lastMidnight + 24 * 60 * 60 * 1000) {
            // Reset step count and update last midnight timestamp
            sharedPreferences.edit().putInt(STEP_COUNT_KEY, 0).putLong(MIDNIGHT_KEY, getTodayMidnight()).apply();
        }
    }

    @NonNull
    private long getTodayMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}