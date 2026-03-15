package com.emergency.patient.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.emergency.patient.R;
import com.emergency.patient.activities.MainActivity;
import com.emergency.patient.activities.QuickAccessActivity;

/**
 * EmergencyBackgroundService — Persistent foreground service.
 *
 * Keeps the app alive in the background and provides a persistent notification
 * for quick access to Medical ID and SOS from the lock screen.
 */
public class EmergencyBackgroundService extends Service {

    public static final String CHANNEL_ID = "emergency_service_channel";
    public static final int NOTIFICATION_ID = 1001;

    private BroadcastReceiver screenOffReceiver;

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ─── Notification ─────────────────────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medical ID & SOS",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Quick access for paramedics & ambulance");
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        // Tapping the notification opens QuickAccessActivity
        Intent quickAccessIntent = new Intent(this, QuickAccessActivity.class);
        quickAccessIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, quickAccessIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Medical ID & Emergency SOS")
                .setContentText("Tap for Paramedic Access or Ambulance")
                .setSmallIcon(R.drawable.ic_sos_cross)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
    }

    // ─── Static Helpers ───────────────────────────────────────────────────────

    /** Call from any Activity to start the service. */
    public static void start(Context context) {
        Intent intent = new Intent(context, EmergencyBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /** Stop the service (e.g. when the user fully logs out). */
    public static void stop(Context context) {
        context.stopService(new Intent(context, EmergencyBackgroundService.class));
    }
}
