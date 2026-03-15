package com.emergency.patient.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.utils.QrGenerator;

import java.util.Calendar;
import java.util.Set;

public class MedicalIdFragment extends Fragment {

    private TextView tvPatientName, tvPatientAge, tvConditionChip;
    private ImageView ivQrCode, ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medical_id, container, false);
        bindViews(view);
        populatePatientData();
        return view;
    }

    private void bindViews(View view) {
        tvPatientName    = view.findViewById(R.id.tv_patient_name_med_id);
        tvPatientAge     = view.findViewById(R.id.tv_patient_age_med_id);
        tvConditionChip  = view.findViewById(R.id.tv_condition_chip_med_id);
        ivQrCode         = view.findViewById(R.id.iv_qr_code_med_id);
        ivAvatar         = view.findViewById(R.id.iv_avatar_med_id);
    }

    private void populatePatientData() {
        if (getContext() == null) return;
        
        // Full name
        String name = TokenManager.getPatientName(getContext());
        tvPatientName.setText(name.isEmpty() ? getString(android.R.string.unknownName) : name);

        // Calculate Age
        long dobMillis = TokenManager.getDOB(getContext());
        if (dobMillis > 0) {
            int age = calculateAge(dobMillis);
            tvPatientAge.setText(getString(R.string.years_old, age));
        } else {
            tvPatientAge.setVisibility(View.GONE);
        }

        // Conditions
        Set<String> conditions = TokenManager.getConditions(getContext());
        if (conditions != null && !conditions.isEmpty()) {
            tvConditionChip.setText(conditions.iterator().next());
            tvConditionChip.setVisibility(View.VISIBLE);
        } else {
            tvConditionChip.setVisibility(View.GONE);
        }

        // Generate and cache QR code
        String uuid = TokenManager.getUUID(getContext());
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
