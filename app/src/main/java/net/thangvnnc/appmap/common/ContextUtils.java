package net.thangvnnc.appmap.common;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class ContextUtils {
    public static Context getContext() {
        try {
            @SuppressLint("PrivateApi")
            Application application = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            return application.getBaseContext();
        }
        catch (Exception ex) {
            throw new UnsupportedOperationException("Null context");
        }
    }
}
