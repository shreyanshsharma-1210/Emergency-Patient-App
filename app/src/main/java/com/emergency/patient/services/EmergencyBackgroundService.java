package com.emergency.patient.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.emergency.patient.R;
import com.emergency.patient.activities.DashboardActivity;
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
        if (!com.emergency.patient.security.TokenManager.isOnboardingComplete(this)) {
            stopSelf();
            return;
        }
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!com.emergency.patient.security.TokenManager.isOnboardingComplete(this)) {
            android.util.Log.d("EmergencyService", "Onboarding not complete, stopping service.");
            stopSelf();
            return START_NOT_STICKY;
        }

        Notification notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        android.util.Log.d("EmergencyService", "Service destroyed.");
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

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Medical ID & Emergency SOS")
                .setContentText("Tap for Paramedic Access or Ambulance")
                .setSmallIcon(R.drawable.ic_sos_cross)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSilent(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();

        // Critical: Set flags for non-dismissible behavior
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        
        return notification;
    }

    // ─── Static Helpers ───────────────────────────────────────────────────────

    /** Call from any Activity to start the service. */
    public static void start(Context context) {
        if (!com.emergency.patient.security.TokenManager.isOnboardingComplete(context)) {
            return;
        }
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
