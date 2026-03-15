package com.emergency.patient.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.services.EmergencyBackgroundService;

import java.util.Date;
import java.util.Set;

public class SettingsFragment extends Fragment {

    private TextView tvName, tvConditions, tvDob, tvBloodGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvName = view.findViewById(R.id.tv_settings_name);
        tvConditions = view.findViewById(R.id.tv_settings_conditions);
        tvDob = view.findViewById(R.id.tv_settings_dob);
        tvBloodGroup = view.findViewById(R.id.tv_settings_blood_group);

        populateData();

        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), EditProfileActivity.class));
        });

        Button btnReset = view.findViewById(R.id.btn_reset_profile);
        btnReset.setOnClickListener(v -> showResetConfirmationDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    private void populateData() {
        if (getContext() == null) return;
        
        String name = TokenManager.getPatientName(getContext());
        tvName.setText(name.isEmpty() ? "Unknown" : name);

        Set<String> conditions = TokenManager.getConditions(getContext());
        if (conditions != null && !conditions.isEmpty()) {
            tvConditions.setText(String.join("\n", conditions));
        } else {
            tvConditions.setText("No conditions listed");
        }

        long dobMillis = TokenManager.getDOB(getContext());
        if (dobMillis > 0) {
            tvDob.setText(DateFormat.format("MMM dd, yyyy", new Date(dobMillis)).toString());
        } else {
            tvDob.setText("Not set");
        }

        // Use TokenManager for blood group as well
        String bloodGroup = TokenManager.getBloodGroup(getContext());
        tvBloodGroup.setText(bloodGroup.isEmpty() ? "Unknown" : bloodGroup);
    }

    private void showResetConfirmationDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Reset Profile & Void QR")
                .setMessage("Resetting your profile will void your current Emergency QR code. You will need to re-verify your health data permanently. Are you sure?")
                .setPositiveButton("Reset & Start Over", (dialog, which) -> {
                    EmergencyBackgroundService.stop(getContext());
                    TokenManager.clearAll(getContext());
                    Intent intent = new Intent(getContext(), Step1BasicInfoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
