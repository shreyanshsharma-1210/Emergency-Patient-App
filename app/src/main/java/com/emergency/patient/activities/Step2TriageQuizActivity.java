package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.emergency.patient.R;
import com.emergency.patient.models.PatientProfile;

public class Step2TriageQuizActivity extends AppCompatActivity {

    private PatientProfile profile;

    private SwitchCompat switchAllergies, switchCardio, switchChronic, switchImplants, switchMeds;
    private EditText etAllergies, etCardio, etChronic, etImplants, etMeds;
    private Button btnNext, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step2_triage_quiz);

        // Retrieve the profile object created in Step 1
        if (getIntent() != null && getIntent().hasExtra("profile_data")) {
            profile = (PatientProfile) getIntent().getSerializableExtra("profile_data");
        } else {
            // Failsafe
            profile = new PatientProfile();
        }

        bindViews();
        setupToggleLogic();
        setupListeners();
    }

    private void bindViews() {
        switchAllergies = findViewById(R.id.switch_allergies);
        switchCardio    = findViewById(R.id.switch_cardio);
        switchChronic   = findViewById(R.id.switch_chronic);
        switchImplants  = findViewById(R.id.switch_implants);
        switchMeds      = findViewById(R.id.switch_meds);

        etAllergies     = findViewById(R.id.et_allergies_details);
        etCardio        = findViewById(R.id.et_cardio_details);
        etChronic       = findViewById(R.id.et_chronic_details);
        etImplants      = findViewById(R.id.et_implants_details);
        etMeds          = findViewById(R.id.et_meds_details);

        btnNext         = findViewById(R.id.btn_next_step2);
        btnBack         = findViewById(R.id.btn_back_step2);
    }

    private void setupToggleLogic() {
        switchAllergies.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etAllergies.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etAllergies.setText("");
        });

        switchCardio.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCardio.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etCardio.setText("");
        });

        switchChronic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etChronic.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etChronic.setText("");
        });

        switchImplants.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etImplants.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etImplants.setText("");
        });

        switchMeds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etMeds.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) etMeds.setText("");
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            // Save triage data into the profile
            profile.setHasSevereAllergies(switchAllergies.isChecked());
            profile.setSevereAllergiesDetails(etAllergies.getText().toString().trim());

            profile.setHasCardioIssues(switchCardio.isChecked());
            profile.setCardioIssuesDetails(etCardio.getText().toString().trim());

            profile.setHasChronicConditions(switchChronic.isChecked());
            profile.setChronicConditionsDetails(etChronic.getText().toString().trim());

            profile.setHasImplants(switchImplants.isChecked());
            profile.setImplantsDetails(etImplants.getText().toString().trim());

            profile.setHasCriticalMeds(switchMeds.isChecked());
            profile.setCriticalMedsDetails(etMeds.getText().toString().trim());

            // Move to Step 3 (Verification)
            Intent intent = new Intent(this, Step3VerifyResumeActivity.class);
            intent.putExtra("profile_data", profile);
            startActivity(intent);
        });
    }
}
