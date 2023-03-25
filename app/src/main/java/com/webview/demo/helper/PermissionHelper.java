package com.webview.demo.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuzongheng on 2016/11/5.
 */

public class PermissionHelper {

    public static boolean hasPermissions(Context context, String... permissionName) {
        for (String permission : permissionName) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static List<String> getGrantedPermissions(Context context, String... permissionName) {
        List<String> permissionsGranted = new ArrayList<>();
        for (String permission : permissionName) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted.add(permission);
            }
        }
        return permissionsGranted;
    }

    public interface CheckPermissionListener {
        void onAllGranted(boolean sync);

        /**
         * Partly granted(deniedPermissions.size() >= 0) or all denied
         */
        void onPartlyGranted(List<String> permissionsDenied, boolean sync);
    }

    public static void CheckPermissions(final Context context, final CheckPermissionListener checkPermissionListener, String... permissionName) {

        if (hasPermissions(context, permissionName)) {
            if (checkPermissionListener != null) {
                checkPermissionListener.onAllGranted(true);
            }
        } else {

            Dexter.withContext(context)
                    .withPermissions(permissionName)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                            if (multiplePermissionsReport != null) {
                                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                                    if (checkPermissionListener != null) {
                                        checkPermissionListener.onAllGranted(false);
                                    }
                                } else {
                                    List<PermissionDeniedResponse> deniedPermissions = multiplePermissionsReport.getDeniedPermissionResponses();
                                    ArrayList<String> permissions = null;
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        permissions.add(deniedPermissions.get(i).getPermissionName());
                                    }
                                    if (checkPermissionListener != null) {
                                        checkPermissionListener.onPartlyGranted(permissions, false);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    })
                    .withErrorListener(new PermissionRequestErrorListener() {
                        @Override
                        public void onError(DexterError error) {
                            Log.e("Dexter", "There was an error: " + error.toString());
                        }
                    })
                    .check();

         /*   AndPermission.with(context)
                    .runtime()
                    .permission(permissionName)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> permissions) {
                            if (checkPermissionListener != null) {
                                checkPermissionListener.onAllGranted(false);
                            }
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> permissions) {
                            if (checkPermissionListener != null) {
                                checkPermissionListener.onPartlyGranted(permissions, false);
                            }
                        }
                    })
                    .start();*/


        }
    }

}
