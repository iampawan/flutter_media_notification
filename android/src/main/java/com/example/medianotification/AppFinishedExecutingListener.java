package com.example.medianotification;

import android.os.AsyncTask;

public class AppFinishedExecutingListener extends AsyncTask<NotificationPanel, Void, Boolean> {
    private NotificationPanel main_activity;

    @Override
    protected Boolean doInBackground(NotificationPanel... params) {
        main_activity = params[0];

        while(!main_activity.isFinishing()) {
            try {
                Thread.sleep(990);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}