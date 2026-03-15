package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.models.PatientProfile;
import com.emergency.patient.network.FcmTokenSyncManager;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.services.EmergencyBackgroundService;
import com.emergency.patient.utils.PermissionHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

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
            tvConditions.setText("- No critical triage conditions reported.");
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

            // Simulate POST request to Node.js backend: /api/patient/register
            new Handler(Looper.getMainLooper()).postDelayed(this::completeRegistration, 1500);
        });
    }

    private void completeRegistration() {
        // Mock backend generating a UUID
        String mockPatientUuid = UUID.randomUUID().toString();

        // Save Core Identifier
        TokenManager.saveUUID(this, mockPatientUuid);

        // Save Demographics to TokenManager
        TokenManager.savePatientName(this, profile.getFullName());
        TokenManager.saveDOB(this, profile.getDobMillis());
        TokenManager.saveGender(this, profile.getGender());
        TokenManager.saveBloodGroup(this, profile.getBloodGroup());

        // Save Condtions
        List<String> activeConditions = profile.getActiveConditionsList();
        Set<String> conditionSet = new HashSet<>(activeConditions);
        if (conditionSet.isEmpty())
            conditionSet.add("Healthy");
        TokenManager.saveConditions(this, conditionSet);

        // Save Emergency Contacts (as JSON string)
        try {
            org.json.JSONArray contactsArr = new org.json.JSONArray();
            for (PatientProfile.EmergencyContact contact : profile.getEmergencyContacts()) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("name", contact.name);
                obj.put("phone", contact.phoneNumber);
                contactsArr.put(obj);
            }
            TokenManager.saveEmergencyContacts(this, contactsArr.toString());
        } catch (org.json.JSONException ignored) {
        }

        // Required App setup
        PermissionHelper.requestAllPermissions(this);
        TokenManager.setOnboardingComplete(this, true);
        FcmTokenSyncManager.syncCurrentToken(this);

        // Start Persistent Notification Service
        EmergencyBackgroundService.start(this);

        Toast.makeText(this, "Profile verified & created!", Toast.LENGTH_SHORT).show();

        // Transition to Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
