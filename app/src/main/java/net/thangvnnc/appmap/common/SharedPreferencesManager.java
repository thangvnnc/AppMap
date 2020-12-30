package net.thangvnnc.appmap.common;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    public static final String SESSION_USER_LOGIN = "SESSION_USER_LOGIN";
    public static final String SELECTED_DIRECTION_ID = "SELECTED_DIRECTION_ID";
    private static final String APP_SETTINGS = "APP_PREFERENCES";

    private SharedPreferencesManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String getString(Context context, String key) {
        return getSharedPreferences(context).getString(key , null);
    }

    public static String getAndRemoveString(Context context, String key) {
        String value = getSharedPreferences(context).getString(key , null);
        removeString(context, key);
        return value;
    }

    public static void setString(Context context, String key, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key , newValue);
        editor.apply();
    }

    public static void removeString(Context context, String key) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(key);
        editor.apply();
    }
}
