package net.huitel.pucapab;

import android.app.Application;
import android.content.Context;

/**
 * Created by root on 1/9/18.
 * Application object so we can have common resources between Activities (such as DAOs to access tables in SQLite database)
 */

public class MainApp extends Application {
    private static Context mContext;

    public static synchronized Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}