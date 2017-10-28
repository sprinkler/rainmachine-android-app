package com.rainmachine.presentation.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;

import com.rainmachine.domain.util.Strings;
import com.rainmachine.infrastructure.util.BaseApplication;

import java.util.List;

/**
 * Common utilities to build Intents for various purposes
 *
 * @author ka
 */
public class IntentUtils {

    @SuppressWarnings("deprecation")
    public static int FLAG_NEW_DOCUMENT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;

    public static Intent newLocationSettingsIntent() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(FLAG_NEW_DOCUMENT);
        return intent;
    }

    /**
     * Ensure that an Activity is available to receive the given Intent
     */
    public static boolean activityExists(Intent intent) {
        final PackageManager mgr = BaseApplication.getContext().getPackageManager();
        final ResolveInfo info = mgr.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (info != null);
    }

    public static Intent newAdvancedWifiIntent() {
        Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
        intent.addFlags(FLAG_NEW_DOCUMENT);
        return intent;
    }

    private static boolean startExternalIntentIfPossible(Context context, Intent intent,
                                                         String preferredPackageName,
                                                         String notAvailable) {
        Context ctx = BaseApplication.getContext();
        List<ResolveInfo> list = ctx.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (!list.isEmpty()) {
            if (!Strings.isBlank(preferredPackageName)) {
                for (ResolveInfo resolveInfo : list) {
                    if (resolveInfo.activityInfo.packageName.equals(preferredPackageName)) {
                        intent.setPackage(preferredPackageName);
                        break;
                    }
                }
            }
            context.startActivity(intent);
            return true;
        }
        if (!Strings.isBlank(notAvailable)) {
            Toasts.show(notAvailable);
        }
        return false;
    }

    public static boolean startExternalIntentIfPossible(Context context, Intent intent) {
        return startExternalIntentIfPossible(context, intent, null, null);
    }
}
