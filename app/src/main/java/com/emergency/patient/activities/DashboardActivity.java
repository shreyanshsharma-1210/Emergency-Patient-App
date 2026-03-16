package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.emergency.patient.R;
import com.emergency.patient.db.AppDatabaseProvider;
import com.emergency.patient.db.PatientEntity;
import com.emergency.patient.network.FcmTokenSyncManager;
import com.emergency.patient.security.TokenManager;

import com.emergency.patient.services.EmergencyBackgroundService;
import com.emergency.patient.utils.PermissionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Route to onboarding if not yet complete
        String uuid = TokenManager.getUUID(this);
        
        new Thread(() -> {
            boolean isComplete;
            try {
                PatientEntity patient = AppDatabaseProvider.getInstance(this).patientDao().getPatient(uuid);
                isComplete = (patient != null && patient.isOnboardingComplete) || TokenManager.isOnboardingComplete(this);
            } catch (Exception e) {
                isComplete = TokenManager.isOnboardingComplete(this);
            }
            
            final boolean finalIsComplete = isComplete;
            runOnUiThread(() -> {
                if (!finalIsComplete) {
                    startActivity(new Intent(this, Step1BasicInfoActivity.class));
                    finish();
                    return;
                }

                requestNotificationPermission();
                PermissionHelper.enforceBatteryOptimizationBypass(this);
                FcmTokenSyncManager.syncCurrentToken(this);

                setContentView(R.layout.activity_main);
                EmergencyBackgroundService.start(this);

                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                bottomNav.setOnItemSelectedListener(item -> {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_health_resume) {
                        selectedFragment = new HealthResumeFragment();
                    } else if (itemId == R.id.nav_medical_id) {
                        selectedFragment = new MedicalIdFragment();
                    } else if (itemId == R.id.nav_settings) {
                        selectedFragment = new SettingsFragment();
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                        return true;
                    }
                    return false;
                });

                // Set default selection
                if (savedInstanceState == null) {
                    bottomNav.setSelectedItemId(R.id.nav_medical_id);
                }
            });
        }).start();
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[] { android.Manifest.permission.POST_NOTIFICATIONS }, 101);
            }
        }
    }
}
