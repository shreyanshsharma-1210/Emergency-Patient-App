package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.utils.PermissionHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * OnboardingActivity — Single-pass patient registration shown only on first launch.
 * Gated by the isOnboardingComplete flag in TokenManager.
 */
public class OnboardingActivity extends AppCompatActivity {

    // Step containers
    private LinearLayout stepOne, stepTwo, stepThree;
    private TextView tvStepIndicator;

    // Step 1 — Identity
    private EditText etFullName, etDob;
    private Spinner spinnerBloodGroup;

    // Step 2 — Emergency Contact
    private EditText etContactName, etContactPhone;

    // Step 3 — Conditions
    private CheckBox cbDiabetes, cbHypertension, cbHeart, cbAsthma, cbOther;

    // Navigation
    private Button btnNext, btnBack;

    private int currentStep = 1;
    private static final int TOTAL_STEPS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip onboarding if already complete
        if (TokenManager.isOnboardingComplete(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        bindViews();
        setupBloodGroupSpinner();
        updateStepVisibility();

        btnNext.setOnClickListener(v -> onNextClicked());
        btnBack.setOnClickListener(v -> onBackClicked());
    }

    private void bindViews() {
        stepOne       = findViewById(R.id.step_one);
        stepTwo       = findViewById(R.id.step_two);
        stepThree     = findViewById(R.id.step_three);
        tvStepIndicator = findViewById(R.id.tv_step_indicator);

        etFullName     = findViewById(R.id.et_full_name);
        etDob          = findViewById(R.id.et_dob);
        spinnerBloodGroup = findViewById(R.id.spinner_blood_group);

        etContactName  = findViewById(R.id.et_contact_name);
        etContactPhone = findViewById(R.id.et_contact_phone);

        cbDiabetes     = findViewById(R.id.cb_diabetes);
        cbHypertension = findViewById(R.id.cb_hypertension);
        cbHeart        = findViewById(R.id.cb_heart);
        cbAsthma       = findViewById(R.id.cb_asthma);
        cbOther        = findViewById(R.id.cb_other);

        btnNext = findViewById(R.id.btn_next);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupBloodGroupSpinner() {
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, bloodGroups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodGroup.setAdapter(adapter);
    }

    private void updateStepVisibility() {
        stepOne.setVisibility(currentStep == 1 ? View.VISIBLE : View.GONE);
        stepTwo.setVisibility(currentStep == 2 ? View.VISIBLE : View.GONE);
        stepThree.setVisibility(currentStep == 3 ? View.VISIBLE : View.GONE);

        tvStepIndicator.setText("Step " + currentStep + " of " + TOTAL_STEPS);
        btnBack.setVisibility(currentStep > 1 ? View.VISIBLE : View.GONE);
        btnNext.setText(currentStep == TOTAL_STEPS ? "Finish" : "Next");
    }

    private void onBackClicked() {
        if (currentStep > 1) {
            currentStep--;
            updateStepVisibility();
        }
    }

    private void onNextClicked() {
        if (!validateCurrentStep()) return;

        if (currentStep < TOTAL_STEPS) {
            currentStep++;
            updateStepVisibility();
        } else {
            completeRegistration();
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 1:
                if (TextUtils.isEmpty(etFullName.getText())) {
                    etFullName.setError("Name is required");
                    return false;
                }
                if (TextUtils.isEmpty(etDob.getText())) {
                    etDob.setError("Date of birth is required");
                    return false;
                }
                break;
            case 2:
                if (TextUtils.isEmpty(etContactName.getText())) {
                    etContactName.setError("Contact name is required");
                    return false;
                }
                if (TextUtils.isEmpty(etContactPhone.getText()) || etContactPhone.getText().length() < 10) {
                    etContactPhone.setError("Valid phone number is required");
                    return false;
                }
                break;
            case 3:
                // Optional: at least one condition can be selected or skipped
                break;
        }
        return true;
    }

    private void completeRegistration() {
        String fullName    = etFullName.getText().toString().trim();
        String dob         = etDob.getText().toString().trim();
        String bloodGroup  = spinnerBloodGroup.getSelectedItem().toString();
        String contactName = etContactName.getText().toString().trim();
        String contactPhone = etContactPhone.getText().toString().trim();
        List<String> conditions = getSelectedConditions();

        // Generate local UUID (backend may override with its own)
        String patientUUID = UUID.randomUUID().toString();
        TokenManager.saveUUID(this, patientUUID);
        TokenManager.savePatientName(this, fullName);

        // TODO: POST registration payload to backend /api/patient/register
        //       Retrieve JWT from response and save via TokenManager.saveJWT()
        //       For now, store a placeholder JWT for development
        TokenManager.saveJWT(this, "PLACEHOLDER_JWT_" + patientUUID);

        // Request all permissions before completing onboarding
        PermissionHelper.requestAllPermissions(this);

        // Mark onboarding complete and go to main screen
        TokenManager.setOnboardingComplete(this, true);

        Toast.makeText(this, "Welcome, " + fullName.split("\\s+")[0] + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private List<String> getSelectedConditions() {
        List<String> conditions = new ArrayList<>();
        if (cbDiabetes.isChecked())     conditions.add("Diabetes");
        if (cbHypertension.isChecked()) conditions.add("Hypertension");
        if (cbHeart.isChecked())        conditions.add("Heart Disease");
        if (cbAsthma.isChecked())       conditions.add("Asthma");
        if (cbOther.isChecked())        conditions.add("Other");
        return conditions;
    }
}
