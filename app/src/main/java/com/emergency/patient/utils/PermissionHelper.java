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

import java.util.ArrayList;
import java.util.List;

/**
 * PermissionHelper — Centralizes all runtime and special permission requests.
 *
 * Dangerous permissions are requested in a single batch.
 * Special permissions (Overlay, Battery Optimization) require Settings intents.
 */
public class PermissionHelper {

    public static final int REQUEST_CODE_PERMISSIONS = 1001;

    private static String[] getDangerousPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return permissions.toArray(new String[0]);
    }

    /**
     * Request all dangerous permissions as a single batch.
     * Call from an Activity; handle results in onRequestPermissionsResult().
     */
    public static void requestDangerousPermissions(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, getDangerousPermissions(), REQUEST_CODE_PERMISSIONS);
    }

    /**
     * Returns true only if every dangerous permission has been granted.
     */
    public static boolean areAllDangerousPermissionsGranted(@NonNull Context context) {
        for (String permission : getDangerousPermissions()) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
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
                Uri.parse("package:" + activity.getPackageName()));
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
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    /**
     * Opens the global battery optimization settings list so the app can be set to
     * "Unrestricted".
     */
    public static void openBatteryOptimizationSettings(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Intent appDetails = new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(appDetails);
        }
    }

    /**
     * Best-effort bypass for personal devices:
     * 1) asks direct exemption
     * 2) opens battery optimization settings as fallback.
     */
    public static void enforceBatteryOptimizationBypass(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        if (isBatteryOptimizationIgnored(activity)) {
            return;
        }
        requestBatteryOptimizationExemption(activity);
        openBatteryOptimizationSettings(activity);
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
            enforceBatteryOptimizationBypass(activity);
        }
    }
}
