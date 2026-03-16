package com.emergency.patient.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.models.PatientProfile;
import com.emergency.patient.network.FcmTokenSyncManager;
import com.emergency.patient.db.AppDatabase;
import com.emergency.patient.db.AppDatabaseProvider;
import com.emergency.patient.db.EmergencyContactEntity;
import com.emergency.patient.db.PatientEntity;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.services.EmergencyBackgroundService;
import com.emergency.patient.utils.PermissionHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class Step3VerifyResumeActivity extends AppCompatActivity {

    private PatientProfile profile;

    private TextView tvName, tvGenderDob, tvBlood, tvConditions, tvVerifyContacts;
    private Button btnBack, btnVerifyCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3_verify_resume);

        if (getIntent() != null && getIntent().hasExtra("profile_data")) {
            profile = (PatientProfile) getIntent().getSerializableExtra("profile_data");
        } else {
            profile = new PatientProfile();
        }

        bindViews();
        populateSummary();
        setupListeners();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tv_verify_name);
        tvGenderDob = findViewById(R.id.tv_verify_gender_dob);
        tvBlood = findViewById(R.id.tv_verify_blood);
        tvConditions = findViewById(R.id.tv_verify_conditions);
        tvVerifyContacts = findViewById(R.id.tv_verify_contacts);

        btnBack = findViewById(R.id.btn_back_step3);
        btnVerifyCreate = findViewById(R.id.btn_verify_create);
    }

    private void populateSummary() {
        tvName.setText(profile.getFullName());

        String dobString = "";
        if (profile.getDobMillis() > 0) {
            dobString = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(new Date(profile.getDobMillis()));
        }
        tvGenderDob.setText((profile.getGender() != null ? profile.getGender() : "") + "  •  " + dobString);

        tvBlood.setText("Blood Group: " + (profile.getBloodGroup() != null ? profile.getBloodGroup() : "Unknown"));

        List<String> conditions = profile.getActiveConditionsList();
        if (conditions.isEmpty()) {
            tvConditions.setText("- No medical history reported.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String c : conditions) {
                sb.append("• ").append(c).append("\n");
            }
            tvConditions.setText(sb.toString().trim());
        }

        // Emergency Contacts
        if (profile.getEmergencyContacts().isEmpty()) {
            tvVerifyContacts.setText("• No emergency contacts provided.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (PatientProfile.EmergencyContact contact : profile.getEmergencyContacts()) {
                sb.append("• ").append(contact.name).append(" (").append(contact.phoneNumber).append(")\n");
            }
            tvVerifyContacts.setText(sb.toString().trim());
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnVerifyCreate.setOnClickListener(v -> {
            btnVerifyCreate.setEnabled(false);
            btnVerifyCreate.setText("Synchronizing...");

            // Simulate registration delay
            new Handler(Looper.getMainLooper()).postDelayed(this::completeRegistration, 1500);
        });
    }

    private void completeRegistration() {
        String mockPatientUuid = UUID.randomUUID().toString();
        TokenManager.saveUUID(this, mockPatientUuid);

        // --- Handle Profile Photo Persistence ---
        String persistentPhotoUri = null;
        if (profile.getProfilePhotoUri() != null) {
            persistentPhotoUri = savePhotoToInternalStorage(profile.getProfilePhotoUri());
            profile.setProfilePhotoUri(persistentPhotoUri);
        }

        // --- Save to Room Database ---
        AppDatabase db = AppDatabaseProvider.getInstance(this);
        
        PatientEntity patient = new PatientEntity(mockPatientUuid);
        patient.fullName = profile.getFullName();
        patient.dobMillis = profile.getDobMillis();
        patient.gender = profile.getGender();
        patient.bloodGroup = profile.getBloodGroup();
        patient.profilePhotoUri = profile.getProfilePhotoUri();
        patient.isOnboardingComplete = true;

        // Medical History fields
        patient.pastMedicalDiagnosis = profile.getPastMedicalDiagnosis();
        patient.pharmacologicalStatus = profile.getPharmacologicalStatus();
        patient.clinicalAllergies = profile.getClinicalAllergies();
        patient.hereditaryConditions = profile.getHereditaryConditions();
        patient.lifestyleFactor = profile.getLifestyleFactor();

        db.patientDao().deleteAllPatients();
        db.patientDao().insertPatient(patient);

        // Save Emergency Contacts
        if (!profile.getEmergencyContacts().isEmpty()) {
            List<EmergencyContactEntity> contactEntities = new java.util.ArrayList<>();
            for (PatientProfile.EmergencyContact c : profile.getEmergencyContacts()) {
                contactEntities.add(new EmergencyContactEntity(mockPatientUuid, c.name, c.phoneNumber));
            }
            db.emergencyContactDao().insertAll(contactEntities);
        }

        // TokenManager backups (for immediate UI updates if needed)
        TokenManager.savePatientName(this, profile.getFullName());
        TokenManager.saveDOB(this, profile.getDobMillis());
        TokenManager.saveGender(this, profile.getGender());
        TokenManager.saveBloodGroup(this, profile.getBloodGroup());

        List<String> activeConditions = profile.getActiveConditionsList();
        Set<String> conditionSet = new HashSet<>(activeConditions);
        if (conditionSet.isEmpty()) conditionSet.add("Healthy");
        TokenManager.saveConditions(this, conditionSet);

        try {
            org.json.JSONArray contactsArr = new org.json.JSONArray();
            for (PatientProfile.EmergencyContact contact : profile.getEmergencyContacts()) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("name", contact.name);
                obj.put("phone", contact.phoneNumber);
                contactsArr.put(obj);
            }
            TokenManager.saveEmergencyContacts(this, contactsArr.toString());
        } catch (org.json.JSONException ignored) {}

        PermissionHelper.requestAllPermissions(this);
        TokenManager.setOnboardingComplete(this, true);
        FcmTokenSyncManager.syncCurrentToken(this);
        EmergencyBackgroundService.start(this);

        Toast.makeText(this, "Profile verified & created!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String savePhotoToInternalStorage(String photoUriString) {
        try {
            Uri sourceUri = Uri.parse(photoUriString);
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File directory = getFilesDir();
            File destFile = new File(directory, "profile_photo.jpg");

            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            Log.d("Step3", "Saved profile photo to: " + destFile.getAbsolutePath());
            return Uri.fromFile(destFile).toString();
        } catch (Exception e) {
            Log.e("Step3", "Failed to save profile photo", e);
            return photoUriString; // Fallback to original URI
        }
    }
}
