package com.emergency.patient.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.emergency.patient.R;
import com.emergency.patient.network.ApiClient;
import com.emergency.patient.security.TokenManager;
import com.emergency.patient.utils.DocumentPickerHelper;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * DocumentUploadActivity — Phase 4 document intake.
 *
 * States: IDLE → SELECTED → UPLOADING → PROCESSING → DONE / ERROR
 *
 * Flow:
 *  1. Patient taps "+ Add Document" — SAF picker opens (PDF / image only)
 *  2. Selected file shown with thumbnail + file name
 *  3. "Upload" → POST multipart/form-data → show progress overlay
 *  4. Backend returns job_id; screen waits for push ("extraction_complete")
 *  5. On notification → launch ValidationActivity with extracted data
 */
public class DocumentUploadActivity extends AppCompatActivity {

    // ─── Views ────────────────────────────────────────────────────────────────
    private LinearLayout layoutIdle, layoutSelected, layoutUploading, layoutError;
    private ImageView ivDocumentPreview;
    private TextView tvDocumentName, tvUploadStatus, tvErrorMessage;
    private Button btnPickDocument, btnUpload, btnRetry;
    private View btnCancel;
    private ProgressBar progressUpload;

    // ─── State ────────────────────────────────────────────────────────────────
    private DocumentPickerHelper picker;
    private Uri pickedUri;
    private File pickedFile;
    private String pickedName;

    // ─── Retrofit API interface ───────────────────────────────────────────────
    interface UploadApi {
        @Multipart
        @POST("api/patient/documents/upload")
        Call<ResponseBody> uploadDocument(
                @Part MultipartBody.Part file,
                @Part("patientUUID") RequestBody uuid
        );
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_upload);
        bindViews();
        showState(State.IDLE);

        picker = new DocumentPickerHelper(this, new DocumentPickerHelper.OnDocumentPicked() {
            @Override
            public void onPicked(Uri uri, String displayName, File cachedCopy) {
                pickedUri  = uri;
                pickedName = displayName;
                pickedFile = cachedCopy;
                onDocumentSelected();
            }

            @Override
            public void onCancelled() { /* stay on IDLE */ }
        });

        btnPickDocument.setOnClickListener(v -> picker.launch());
        btnUpload.setOnClickListener(v -> startUpload());
        btnRetry.setOnClickListener(v -> showState(State.IDLE));
        btnCancel.setOnClickListener(v -> finish());
    }

    // ─── Document Selected ────────────────────────────────────────────────────

    private void onDocumentSelected() {
        tvDocumentName.setText(pickedName);

        // Show image preview if applicable
        String mime = DocumentPickerHelper.getMimeType(this, pickedUri);
        if (mime.startsWith("image/")) {
            ivDocumentPreview.setImageURI(pickedUri);
            ivDocumentPreview.setVisibility(View.VISIBLE);
        } else {
            // PDF — show generic document icon
            ivDocumentPreview.setImageResource(android.R.drawable.ic_menu_agenda);
            ivDocumentPreview.setVisibility(View.VISIBLE);
        }
        showState(State.SELECTED);
    }

    // ─── Upload ───────────────────────────────────────────────────────────────

    private void startUpload() {
        if (pickedFile == null || !pickedFile.exists()) {
            showError("Document file not accessible. Please pick again.");
            return;
        }
        showState(State.UPLOADING);
        tvUploadStatus.setText("Uploading…");

        String uuid = TokenManager.getUUID(this);
        String mime = DocumentPickerHelper.getMimeType(this, pickedUri);

        RequestBody fileBody = RequestBody.create(pickedFile, MediaType.parse(mime));
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "document", pickedFile.getName(), fileBody);
        RequestBody uuidBody = RequestBody.create(uuid, MediaType.parse("text/plain"));

        UploadApi api = ApiClient.getInstance(this).create(UploadApi.class);
        api.uploadDocument(filePart, uuidBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Backend accepted upload — now polling for extraction_complete push
                    tvUploadStatus.setText("Processing by AI…");
                    // TODO: Register FCM listener; navigate when push arrives
                    // For now simulate success after delay
                    progressUpload.postDelayed(
                            () -> launchValidation("{\"job_id\":\"pending\"}"),
                            2_000);
                } else {
                    showError("Upload failed (" + response.code() + "). Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void launchValidation(String extractionPayload) {
        android.content.Intent intent =
                new android.content.Intent(this, ValidationActivity.class);
        intent.putExtra(ValidationActivity.EXTRA_PAYLOAD,  extractionPayload);
        intent.putExtra(ValidationActivity.EXTRA_DOC_NAME, pickedName);
        startActivity(intent);
        finish();
    }

    // ─── Error ────────────────────────────────────────────────────────────────

    private void showError(String message) {
        tvErrorMessage.setText(message);
        showState(State.ERROR);
    }

    // ─── State Machine ────────────────────────────────────────────────────────

    private enum State { IDLE, SELECTED, UPLOADING, ERROR }

    private void showState(State state) {
        layoutIdle.setVisibility(    state == State.IDLE      ? View.VISIBLE : View.GONE);
        layoutSelected.setVisibility(state == State.SELECTED  ? View.VISIBLE : View.GONE);
        layoutUploading.setVisibility(state == State.UPLOADING ? View.VISIBLE : View.GONE);
        layoutError.setVisibility(   state == State.ERROR     ? View.VISIBLE : View.GONE);
    }

    // ─── View Binding ─────────────────────────────────────────────────────────

    private void bindViews() {
        layoutIdle      = findViewById(R.id.layout_idle);
        layoutSelected  = findViewById(R.id.layout_selected);
        layoutUploading = findViewById(R.id.layout_uploading);
        layoutError     = findViewById(R.id.layout_error);

        ivDocumentPreview = findViewById(R.id.iv_doc_preview);
        tvDocumentName    = findViewById(R.id.tv_doc_name);
        tvUploadStatus    = findViewById(R.id.tv_upload_status);
        tvErrorMessage    = findViewById(R.id.tv_error_message);

        btnPickDocument   = findViewById(R.id.btn_pick_document);
        btnUpload         = findViewById(R.id.btn_upload);
        btnRetry          = findViewById(R.id.btn_retry);
        btnCancel         = findViewById(R.id.btn_cancel_upload);

        progressUpload    = findViewById(R.id.progress_upload);
    }
}
