package com.vivoka.freespeech.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

public class PermissionUtils {

    // All permissions
    public static void requestAllPermissions(AppCompatActivity activity, int requestId) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, requestId);
    }

    public static void showPermissionsNotEnableDialog(final Context context, final String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enable permissions")
                .setMessage("Please enable permissions to continue to use this app")
                .setCancelable(false)
                .setPositiveButton("Enable now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri  = Uri.fromParts("package", packageName, null);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                }).show();
    }

    // Record Audio
    public static boolean isRecordAudioGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }
}
