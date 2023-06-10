package com.example.notesapp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

/**
 * @author 30415
 */
public class PermissionUtils {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_MEDIA = 2;
    private static final String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private static final String[] PERMISSIONS_MEDIA = {"android.permission.READ_MEDIA_IMAGES"};

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission == PackageManager.PERMISSION_DENIED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            int permission1 = ActivityCompat.checkSelfPermission(activity, "android.permission.READ_MEDIA_IMAGES");
            if (permission1 == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_MEDIA, REQUEST_MEDIA);
            }
        } catch (Exception e) {
            Toast.makeText(activity.getApplicationContext(), "???", Toast.LENGTH_SHORT).show();
        }
    }

}
