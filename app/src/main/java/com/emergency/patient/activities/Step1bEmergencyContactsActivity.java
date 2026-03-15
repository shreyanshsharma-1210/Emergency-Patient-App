package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.models.PatientProfile;

public class Step1bEmergencyContactsActivity extends AppCompatActivity {

    private PatientProfile profile;

    private EditText etName1, etPhone1;
    private EditText etName2, etPhone2;
    private EditText etName3, etPhone3;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1b_emergency_contacts);

        if (getIntent() != null && getIntent().hasExtra("profile_data")) {
            profile = (PatientProfile) getIntent().getSerializableExtra("profile_data");
        } else {
            profile = new PatientProfile();
        }

        bindViews();
        setupListeners();
    }

    private void bindViews() {
        etName1 = findViewById(R.id.et_contact1_name);
        etPhone1 = findViewById(R.id.et_contact1_phone);
        etName2 = findViewById(R.id.et_contact2_name);
        etPhone2 = findViewById(R.id.et_contact2_phone);
        etName3 = findViewById(R.id.et_contact3_name);
        etPhone3 = findViewById(R.id.et_contact3_phone);
        btnNext = findViewById(R.id.btn_next_step1b);
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (validateAndSave()) {
                Intent intent = new Intent(this, Step2TriageQuizActivity.class);
                intent.putExtra("profile_data", profile);
                startActivity(intent);
            }
        });
    }

    private boolean validateAndSave() {
        String name1 = etName1.getText().toString().trim();
        String phone1 = etPhone1.getText().toString().trim();
        String name2 = etName2.getText().toString().trim();
        String phone2 = etPhone2.getText().toString().trim();
        String name3 = etName3.getText().toString().trim();
        String phone3 = etPhone3.getText().toString().trim();

        if (TextUtils.isEmpty(name1) || TextUtils.isEmpty(phone1)) {
            Toast.makeText(this, "Please provide at least one primary emergency contact.", Toast.LENGTH_SHORT).show();
            return false;
        }

        profile.getEmergencyContacts().clear();
        profile.addEmergencyContact(name1, phone1);

        if (!TextUtils.isEmpty(name2) && !TextUtils.isEmpty(phone2)) {
            profile.addEmergencyContact(name2, phone2);
        }

        if (!TextUtils.isEmpty(name3) && !TextUtils.isEmpty(phone3)) {
            profile.addEmergencyContact(name3, phone3);
        }

        return true;
    }
}
