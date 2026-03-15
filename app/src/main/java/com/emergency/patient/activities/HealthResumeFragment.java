package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emergency.patient.R;
import com.emergency.patient.security.TokenManager;
import java.util.Set;

public class HealthResumeFragment extends Fragment {

    private TextView tvName, tvBlood, tvConditions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_health_resume, container, false);

        tvName = view.findViewById(R.id.tv_resume_patient_name);
        tvBlood = view.findViewById(R.id.tv_resume_blood_group);
        tvConditions = view.findViewById(R.id.tv_resume_conditions);

        populateSummary();

        Button btnUpload = view.findViewById(R.id.btn_upload_document);
        btnUpload.setOnClickListener(v -> {
            if (getContext() != null) {
                startActivity(new Intent(getContext(), DocumentUploadActivity.class));
            }
        });

        return view;
    }

    private void populateSummary() {
        if (getContext() == null) return;

        tvName.setText(TokenManager.getPatientName(getContext()));
        tvBlood.setText("Blood Group: " + TokenManager.getBloodGroup(getContext()));

        Set<String> conditions = TokenManager.getConditions(getContext());
        if (conditions.isEmpty() || (conditions.size() == 1 && conditions.contains("Healthy"))) {
            tvConditions.setText("- No critical triage conditions reported.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String c : conditions) {
                if (!c.equals("Healthy")) { // Only append non-healthy conditions
                    sb.append("• ").append(c).append("\n");
                }
            }
            tvConditions.setText(sb.toString().trim());
        }
    }
}
