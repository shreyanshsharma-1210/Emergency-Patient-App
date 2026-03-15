package com.emergency.patient.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.emergency.patient.R;
import com.emergency.patient.activities.ValidationActivity;
import com.emergency.patient.network.ApiClient;
import com.emergency.patient.network.ApiService;
import com.emergency.patient.security.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AppFirebaseMessagingService — Handles incoming FCM push notifications.
 *
 * Expected payload (from backend) when AI processing completes:
 * {
 *   "data": {
 *     "type":          "extraction_complete",
 *     "processing_id": "abc-123",
 *     "status":        "success",
 *     "payload":       "{...extracted JSON...}"  // optional inline payload
 *   }
 * }
 */
public class AppFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG              = "FCMService";
    private static final String CHANNEL_ID       = "emergency_extraction_channel";
    private static final int    NOTIFICATION_ID  = 9002;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed: " + token.substring(0, Math.min(token.length(), 10)) + "...");
        uploadTokenToBackend(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        if (message.getData().isEmpty()) return;

        String type        = message.getData().get("type");
        String processingId = message.getData().get("processing_id");
        String status      = message.getData().get("status");
        String payload     = message.getData().get("payload");

        Log.d(TAG, "FCM received: type=" + type + " id=" + processingId + " status=" + status);

        if ("extraction_complete".equals(type) && "success".equals(status)) {
            showExtractionNotification(processingId, payload);
        }
    }

    // ─── Notification ─────────────────────────────────────────────────────────

    private void showExtractionNotification(String processingId, String payload) {
        ensureChannel();

        // Tap → opens ValidationActivity with extraction data
        Intent intent = new Intent(this, ValidationActivity.class);
        intent.putExtra(ValidationActivity.EXTRA_PAYLOAD,  payload);
        intent.putExtra(ValidationActivity.EXTRA_DOC_NAME, "Medical Report");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sos_cross)
                .setContentTitle("Medical Report Processed ✅")
                .setContentText("Tap to review and approve the extracted data.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFICATION_ID, builder.build());
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "AI Extraction Results",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when your medical document has been processed");
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void uploadTokenToBackend(String token) {
        if (token == null || token.isEmpty()) return;

        ApiService api = ApiClient.getInstance(this).create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", token);
        body.put("patientUUID", TokenManager.getUUID(this));

        api.uploadFcmToken(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token uploaded successfully ✅");
                } else {
                    Log.w(TAG, "FCM token upload failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "FCM token upload failed", t);
            }
        });
    }
}
