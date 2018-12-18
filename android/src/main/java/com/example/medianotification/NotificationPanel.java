package com.example.medianotification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.util.Log;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationPanel extends Activity {
    private static final String TAG = "MediaNotificationPanel";
    private static final int NOTIFICATION_ID = 1565461;
    Timer t;
    private Context parent;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;
    private String title;
    private String author;
    private boolean play;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    private AudioManager audioManager;
    PowerManager powerManager;

    public NotificationPanel(Context parent, String title, String author, boolean play) {
        this.parent = parent;
        this.title = title;
        this.author = author;
        this.play = play;

        Intent dismissIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("dismiss");
        PendingIntent pendingDismissIntent = PendingIntent.getBroadcast(parent, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        nBuilder = new NotificationCompat.Builder(parent, "media_notification")
                .setOngoing(play)
                .setSmallIcon(R.drawable.ic_stat_music_note)
                .setVibrate(new long[]{0L})
                .setSound(null)
                .setDeleteIntent(pendingDismissIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            nBuilder.setPriority(Notification.STREAM_DEFAULT);
        }

        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notificationlayout);

        remoteView.setTextViewText(R.id.title, title);
        remoteView.setTextViewText(R.id.author, author);

        if (this.play) {
            remoteView.setImageViewResource(R.id.toggle, R.drawable.baseline_pause_black_48);
        } else {
            remoteView.setImageViewResource(R.id.toggle, R.drawable.baseline_play_arrow_black_48);
        }

        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        Notification notification = nBuilder.build();

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, notification);
    }

    private void requestWifiLock() {
        if (Build.VERSION.SDK_INT < 3) {
            Log.i(TAG, "Sæki ekki WifiLock, build version er: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        try {
            if (wifiLock == null) {
                WifiManager wifiManager = ((WifiManager)parent.getApplicationContext().getSystemService(Context.WIFI_SERVICE));
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "ruv.gardina:wifilock");
                wifiLock.setReferenceCounted(false);
            }
            if (!wifiLock.isHeld()) {
                wifiLock.acquire();
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Fékk ekki WifiLock: " + e.getLocalizedMessage());
        }
    }

    private void releaseWifiLock() {
        if (Build.VERSION.SDK_INT < 3) {
            return;
        }
        if (wifiLock == null) {
            return;
        }
        try {
            if (wifiLock.isHeld()) {
                wifiLock.release();
                wifiLock = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Released ekki WifiLock: " + e.getLocalizedMessage());
        }
    }

    private void requestWakeLock() {
        if (Build.VERSION.SDK_INT < 1) {
            Log.i(TAG, "Sæki ekki WakeLock, build version er: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        try {
            if (powerManager == null) {
                powerManager = (PowerManager)parent.getSystemService(Context.POWER_SERVICE);
            }
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ruv.WakeLock.player:wakeLock");
                wakeLock.setReferenceCounted(false);
            }
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Fékk ekki WakeLock: " + e.getLocalizedMessage());
        }
    }

    private void releaseWakeLock() {
        if (Build.VERSION.SDK_INT < 1) {
            return;
        }
        if (wakeLock == null) {
            return;
        }
        try {
            if (wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Released ekki WakeLock: " + e.getLocalizedMessage());
        }
    }

    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT < 8) {
            Log.i(TAG, "Sæki ekki audioFocus, build version er: " + Integer.toString(Build.VERSION.SDK_INT));
            return;
        }
        try {
            if (audioManager == null) {
                audioManager = (AudioManager) parent.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            }
            int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == audioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.i(TAG, "Fékk audioFocus");
            } else {
                Log.i(TAG, "Fékk ekki audioFocus");
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Fékk ekki audioFocus: " + e.getLocalizedMessage());
        }
    }

    private void releaseAudioFocus() {
        if (Build.VERSION.SDK_INT < 8) {
            return;
        }
        if (audioManager == null) {
            return;
        }
        try {
            if (!audioManager.isMusicActive()) {
                audioManager.abandonAudioFocus(null);
                audioManager = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "THROW - Released ekki AudioFocus: " + e.getLocalizedMessage());
        }
    }

    public void setListeners(RemoteViews view){
        Intent intent = new Intent(parent, NotificationReturnSlot.class)
            .setAction("toggle")
            .putExtra("title", this.title)
            .putExtra("author", this.author)
            .putExtra("action", !this.play ? "play" : "pause");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(parent, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.toggle, pendingIntent);

        Intent nextIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("next");
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(parent, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.next, pendingNextIntent);

        Intent prevIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("prev");
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(parent, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.prev, pendingPrevIntent);

        Intent selectIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("select");
        PendingIntent selectPendingIntent = PendingIntent.getBroadcast(parent, 0, selectIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        view.setOnClickPendingIntent(R.id.layout, selectPendingIntent);
    }

    public void notificationCancel() {
        nManager.cancel(NOTIFICATION_ID);
    }

    /*
    public void closeNotificationIfNotRunning() {
        Log.i(TAG, "Gísli's Heartbeat");
        //return;

        boolean running = isAppRunning(parent);
        if (!running) {
            // Remove the notification
            releaseLocks();
            notificationCancel();
        }
    }
    */

    public void stopSound() {
        requestAudioFocus();
    }

    public void getWifiLock() {
        // Vinn med hljóð í bakgrunni
        requestWakeLock();
        requestAudioFocus();
        requestWifiLock();

        /*
        // Setup listener
        if (t == null) {
            t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    closeNotificationIfNotRunning();
                }
            }, 1000, 1000);
        }
        */
    }

    public void releaseLocks() {
        releaseWakeLock();
        releaseAudioFocus();
        releaseWifiLock();
        /*
        if (t != null) {
            t.cancel();
            t = null;
        }
        */
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "Media Gardina onDestroy");
        nManager.cancel(NOTIFICATION_ID);
        releaseLocks();
        //t.cancel();
        super.onDestroy();
    }

    void select() {
        if (audioManager != null) {
            if (audioManager.isMusicActive()) {
                return;
            }
        }
        releaseLocks();
        notificationCancel();
    }

    /*
    private boolean isAppRunning(Context context) {
        ActivityManager m = (ActivityManager) context.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
        Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
        int n=0;
        while(itr.hasNext()){
            n++;
            itr.next();
        }
        if(n==1){ // App is killed
            return false;
        }
        return true; // App is in background or foreground
    }
    */
}