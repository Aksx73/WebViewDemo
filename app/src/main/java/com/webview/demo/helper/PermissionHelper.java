package com.webview.demo.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
                    .start();
                    */

            PermissionX.init((FragmentActivity) context)
                    .permissions(permissionName)
                    .onExplainRequestReason((scope, deniedList) -> {
                        scope.showRequestReasonDialog(deniedList, "Core fundamental are based on these permissions", "OK", "Cancel");
                    })
                    .onForwardToSettings((scope, deniedList) -> {
                        scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel");
                    })
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            if (checkPermissionListener != null) {
                                checkPermissionListener.onAllGranted(false);
                            }
                        }
                        else {
                            if (checkPermissionListener != null) {
                                checkPermissionListener.onPartlyGranted(deniedList, false);
                            }
                        }
                    });

            //todo ISSUE: dialog on permission denial not shown

        }
    }

}
