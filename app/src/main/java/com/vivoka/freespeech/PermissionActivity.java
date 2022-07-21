package com.vivoka.freespeech;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vivoka.freespeech.utils.PermissionUtils;


public class PermissionActivity extends AppCompatActivity {

    // Permissions
    private final int REQUEST_PERMISSIONS_ALL = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_permission);
    }


    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
    }


    private void requestPermissions() {
        if (PermissionUtils.isRecordAudioGranted(this)) {
            gotoNextActivity();
            return;
        }

        PermissionUtils.requestAllPermissions(this, REQUEST_PERMISSIONS_ALL);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS_ALL) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gotoNextActivity();
                } else {
                    PermissionUtils.showPermissionsNotEnableDialog(this, getPackageName());
                }
            }
        }
    }


    private void gotoNextActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
