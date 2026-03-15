package com.emergency.patient.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName;
    private TextView tvDob, tvBloodGroup;
    private Spinner spinnerGender;
    private Button btnSave;
    private ImageButton btnClose;

    private long selectedDobMillis;
    private String[] genders = {"Male", "Female", "Other", "Prefer not to say"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        bindViews();
        setupGenderSpinner();
        loadExistingData();
        setupListeners();
    }

    private void bindViews() {
        etName = findViewById(R.id.et_edit_name);
        tvDob = findViewById(R.id.tv_edit_dob);
        tvBloodGroup = findViewById(R.id.tv_edit_blood_group);
        spinnerGender = findViewById(R.id.spinner_edit_gender);
        btnSave = findViewById(R.id.btn_save_profile);
        btnClose = findViewById(R.id.btn_close_edit);
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadExistingData() {
        etName.setText(TokenManager.getPatientName(this));
        
        selectedDobMillis = TokenManager.getDOB(this);
        if (selectedDobMillis > 0) {
            updateDobDisplay();
        }

        SharedPreferences authPrefs = getSharedPreferences("patient_auth_prefs", MODE_PRIVATE);
        String gender = authPrefs.getString("gender", "");
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(gender)) {
                spinnerGender.setSelection(i);
                break;
            }
        }

        tvBloodGroup.setText(authPrefs.getString("blood_group", "Unknown"));
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        
        tvDob.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            if (saveChanges()) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDobMillis > 0) calendar.setTimeInMillis(selectedDobMillis);

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedDobMillis = calendar.getTimeInMillis();
            updateDobDisplay();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDobDisplay() {
        String dobString = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(selectedDobMillis));
        tvDob.setText(dobString);
    }

    private boolean saveChanges() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        TokenManager.savePatientName(this, name);
        TokenManager.saveDOB(this, selectedDobMillis);
        
        String gender = spinnerGender.getSelectedItem().toString();
        getSharedPreferences("patient_auth_prefs", MODE_PRIVATE)
                .edit()
                .putString("gender", gender)
                .apply();

        return true;
    }
}
