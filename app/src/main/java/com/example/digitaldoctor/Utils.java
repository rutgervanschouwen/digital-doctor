package com.example.digitaldoctor;

import static android.content.Context.MODE_PRIVATE;
import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.digitaldoctor.models.Session;

public class Utils {

    public static Session setSharedPreferences(Context c, SessionDao dao) {

        Session session;
        SharedPreferences sharedPref = c.getSharedPreferences("SharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Check if there is already a session, if not, create one
        String interviewId = sharedPref.getString("Interview-Id", null);
        if (interviewId == null || dao.getSession(interviewId) == null) {
            session = null;
        } else {
            session = dao.getSession(interviewId);
        }

        return session;
    }

    public static boolean isDarkModeEnabled(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 (API level 29) and above, use system-wide dark mode setting
            Configuration configuration = c.getResources().getConfiguration();
            return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        } else {
            // For versions below Android 10, use deprecated UiModeManager
            UiModeManager uiModeManager = (UiModeManager) c.getSystemService(Context.UI_MODE_SERVICE);
            return uiModeManager != null && uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
        }
    }

    public static void disableNextBtn(Context c, Button button) {
        if (isDarkModeEnabled(c)) {
            button.setBackgroundColor(c.getResources().getColor(R.color.dark));
            button.setTextColor(c.getResources().getColor(R.color.dark_2));
        } else {
            button.setBackgroundColor(c.getResources().getColor(R.color.btn_disabled));
            button.setTextColor(c.getResources().getColor(R.color.btn_disabled_text));
        }
    }

    public static void resetInterview(SessionDao dao, String sessionId) {
        // Clear all the evidence from the session, so the interview starts over
        Session session = dao.getSession(sessionId);
        session.evidenceList.clear();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                dao.updateSession(session);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG).show();
        return false;
    }

}
