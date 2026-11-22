package com.gameye98.osint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    private final Activity activity;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    public PermissionUtils(Activity activity) {
        this.activity = activity;
    }

    public void checkAllPermissions() {
        if (activity == null) return;

        // On modern Android (API 30+), Scoped Storage is enforced.
        // On Android 13+ (API 33+), storage permissions are partitioned or handled via system pickers.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            String[] storagePermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            for (String perm : storagePermissions) {
                requestPermissionIfNeeded(activity, perm);
            }
        }
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        if (context == null || permission == null) return true;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissionIfNeeded(Activity activity, String permission) {
        if (activity == null || permission == null) return;
        if (!isPermissionGranted(activity, permission)) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
        }
    }
}
