package com.raywenderlich.placebook;

import android.app.Application;
import android.content.Context;

public class MainContext extends Application {

        private static Context context;

        public void onCreate() {
            super.onCreate();
            MainContext.context = getApplicationContext();
        }

        public static Context getAppContext() {
            return MainContext.context;
        }

}
