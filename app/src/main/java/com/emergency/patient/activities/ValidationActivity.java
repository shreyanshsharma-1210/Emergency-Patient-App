package com.emergency.patient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.network.ApiClient;
import com.emergency.patient.security.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * ValidationActivity — Human-in-the-loop review of AI-extracted medical data.
 *
 * Receives the extraction payload from backend (via intent or FCM notification).
 * Displays all extracted fields as editable EditTexts alongside the document name.
 * Patient must explicitly tap "Approve & Save" to commit data — NO auto-commits.
 *
 * Design principle: AI is a helper, not the authority. Every field is editable.
 */
public class ValidationActivity extends AppCompatActivity {

    public static final String EXTRA_PAYLOAD  = "extra_extraction_payload";
    public static final String EXTRA_DOC_NAME = "extra_doc_name";

    // ─── Views ────────────────────────────────────────────────────────────────
    private TextView tvDocName, tvExtractedHeader;
    private LinearLayout llFieldsContainer;
    private Button btnApprove, btnReject;

    // ─── Data ─────────────────────────────────────────────────────────────────
    private JSONObject extractedData;
    private String docName;

    // ─── Commit API ───────────────────────────────────────────────────────────
    interface CommitApi {
        @POST("api/patient/profile/update")
        Call<ResponseBody> commitProfile(@Body RequestBody body);
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);
        bindViews();

        String payload = getIntent().getStringExtra(EXTRA_PAYLOAD);
        docName        = getIntent().getStringExtra(EXTRA_DOC_NAME);

        if (docName != null) tvDocName.setText("From: " + docName);

        if (payload != null) {
            try {
                extractedData = new JSONObject(payload);
                populateFields(extractedData);
            } catch (JSONException e) {
                tvExtractedHeader.setText("⚠ Could not parse extraction data.");
            }
        }

        btnApprove.setOnClickListener(v -> commitApprovedData());
        btnReject.setOnClickListener(v -> {
            // Discard everything — AI data is never applied without consent
            Toast.makeText(this, "Extraction discarded.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    // ─── Field Population ─────────────────────────────────────────────────────

    /**
     * Dynamically creates one EditText per extracted field so the patient
     * can correct any AI mistakes before approval.
     */
    private void populateFields(JSONObject data) {
        llFieldsContainer.removeAllViews();
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value;
            try { value = data.getString(key); } catch (JSONException e) { value = ""; }

            // Label
            TextView label = new TextView(this);
            label.setText(keyToLabel(key));
            label.setTextSize(12f);
            label.setTextColor(0xFF8A96A3);
            label.setPadding(0, 16, 0, 4);
            llFieldsContainer.addView(label);

            // Editable field — pre-filled with extracted value
            EditText field = new EditText(this);
            field.setTag(key);                    // tag = JSON key for commit
            field.setText(value);
            field.setTextSize(15f);
            field.setTextColor(0xFF1A2B3C);
            field.setBackgroundResource(android.R.drawable.edit_text);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = 4;
            field.setLayoutParams(params);
            llFieldsContainer.addView(field);
        }
    }

    /** Converts camelCase JSON key to readable label. */
    private String keyToLabel(String key) {
        String spaced = key.replaceAll("([A-Z])", " $1");
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    // ─── Commit ───────────────────────────────────────────────────────────────

    private void commitApprovedData() {
        // Collect all (potentially edited) values from EditTexts
        JSONObject approved = new JSONObject();
        for (int i = 0; i < llFieldsContainer.getChildCount(); i++) {
            View child = llFieldsContainer.getChildAt(i);
            if (child instanceof EditText) {
                EditText et = (EditText) child;
                String key  = (String) et.getTag();
                String val  = et.getText().toString().trim();
                try { approved.put(key, val); } catch (JSONException ignored) {}
            }
        }

        // Add patient UUID as identifier
        try { approved.put("patientUUID", TokenManager.getUUID(this)); }
        catch (JSONException ignored) {}

        // POST to backend
        RequestBody body = RequestBody.create(
                approved.toString(), MediaType.parse("application/json"));

        CommitApi api = ApiClient.getInstance(this).create(CommitApi.class);
        api.commitProfile(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ValidationActivity.this,
                            "Medical profile updated ✅", Toast.LENGTH_SHORT).show();
                    // Return to main screen (Dashboard)
                    Intent intent = new Intent(ValidationActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ValidationActivity.this,
                            "Save failed (" + response.code() + "). Please retry.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ValidationActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ─── View Binding ─────────────────────────────────────────────────────────

    private void bindViews() {
        tvDocName         = findViewById(R.id.tv_doc_name_validation);
        tvExtractedHeader = findViewById(R.id.tv_extracted_header);
        llFieldsContainer = findViewById(R.id.ll_fields_container);
        btnApprove        = findViewById(R.id.btn_approve_save);
        btnReject         = findViewById(R.id.btn_reject_discard);
    }
}
