package com.emergency.patient.activities;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.services.EmergencyBackgroundService;
import com.emergency.patient.utils.QrGenerator;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * MainActivity — The primary dashboard of the app.
 * Shows the Medical ID screen including patient profile card and QR code.
 * Routes to OnboardingActivity on first launch.
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvPatientName, tvPatientAge, tvConditionChip, btnEdit;
    private ImageView ivQrCode, ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Route to onboarding if not yet complete
        if (!TokenManager.isOnboardingComplete(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        // Ensure the background service is running whenever the app is open
        EmergencyBackgroundService.start(this);
        bindViews();
        populatePatientData();
    }

    private void bindViews() {
        tvPatientName    = findViewById(R.id.tv_patient_name);
        tvPatientAge     = findViewById(R.id.tv_patient_age);
        tvConditionChip  = findViewById(R.id.tv_condition_chip);
        ivQrCode         = findViewById(R.id.iv_qr_code);
        ivAvatar         = findViewById(R.id.iv_avatar);
        btnEdit          = findViewById(R.id.btn_edit);

        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(this, DocumentUploadActivity.class));
        });
    }

    private void populatePatientData() {
        // Full name
        String name = TokenManager.getPatientName(this);
        tvPatientName.setText(name.isEmpty() ? getString(android.R.string.unknownName) : name);

        // Calculate Age
        long dobMillis = TokenManager.getDOB(this);
        if (dobMillis > 0) {
            int age = calculateAge(dobMillis);
            tvPatientAge.setText(getString(R.string.years_old, age));
        } else {
            tvPatientAge.setVisibility(android.view.View.GONE);
        }

        // Conditions
        Set<String> conditions = TokenManager.getConditions(this);
        if (conditions != null && !conditions.isEmpty()) {
            // MVP shows the first significant condition
            tvConditionChip.setText(conditions.iterator().next());
            tvConditionChip.setVisibility(android.view.View.VISIBLE);
        } else {
            tvConditionChip.setVisibility(android.view.View.GONE);
        }

        // Generate and cache QR code
        String uuid = TokenManager.getUUID(this);
        if (uuid != null) {
            Bitmap qrBitmap = QrGenerator.generate(uuid, 280);
            if (qrBitmap != null) {
                ivQrCode.setImageBitmap(qrBitmap);
            }
        }
    }

    private int calculateAge(long dobMillis) {
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(dobMillis);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}
