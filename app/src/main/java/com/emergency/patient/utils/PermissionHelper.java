package com.emergency.patient.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * PermissionHelper — Centralizes all runtime and special permission requests.
 *
 * Dangerous permissions are requested in a single batch.
 * Special permissions (Overlay, Battery Optimization) require Settings intents.
 */
public class PermissionHelper {

    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    /** Dangerous permissions that can be batch-requested at runtime. */
    private static final String[] DANGEROUS_PERMISSIONS = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE
    };

    /**
     * Request all dangerous permissions as a single batch.
     * Call from an Activity; handle results in onRequestPermissionsResult().
     */
    public static void requestDangerousPermissions(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, DANGEROUS_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    /**
     * Returns true only if every dangerous permission has been granted.
     */
    public static boolean areAllDangerousPermissionsGranted(@NonNull Context context) {
        for (String permission : DANGEROUS_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // ─── System Overlay (SYSTEM_ALERT_WINDOW) ────────────────────────────────

    /** Returns true if the app can draw overlays (needed for Lock Screen). */
    public static boolean canDrawOverlays(@NonNull Context context) {
        return Settings.canDrawOverlays(context);
    }

    /**
     * Opens the Settings page for overlay permission.
     * Requires manual user approval — cannot be requested via requestPermissions().
     */
    public static void requestOverlayPermission(@NonNull Activity activity) {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName())
        );
        activity.startActivity(intent);
    }

    // ─── Battery Optimization Exemption ──────────────────────────────────────

    /** Returns true if the app is excluded from battery optimization. */
    public static boolean isBatteryOptimizationIgnored(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }

    /**
     * Opens the Settings page to request battery optimization exemption.
     * Required to keep EmergencyBackgroundService alive.
     */
    public static void requestBatteryOptimizationExemption(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + activity.getPackageName())
            );
            activity.startActivity(intent);
        }
    }

    // ─── Convenience: Request All ─────────────────────────────────────────────

    /**
     * Requests all permissions in the correct order.
     * 1. Dangerous permissions (batch).
     * 2. Overlay permission (if not granted).
     * 3. Battery optimization (if not granted).
     */
    public static void requestAllPermissions(@NonNull Activity activity) {
        requestDangerousPermissions(activity);
        if (!canDrawOverlays(activity)) {
            requestOverlayPermission(activity);
        }
        if (!isBatteryOptimizationIgnored(activity)) {
            requestBatteryOptimizationExemption(activity);
        }
    }
}
