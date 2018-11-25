package com.example.medianotification;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static android.content.Context.ACTIVITY_SERVICE;


public class NotificationPanel extends Activity {
    private static NotificationPanel shared = null;
    private static final int NOTIFICATION_ID = 1565461;
    private AppFinishedExecutingListener appFinishedExecutingListener;
    Timer t = new Timer();
    private Context parent;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;
    private String title;
    private String author;
    private boolean play;

    public NotificationPanel(Context parent, String title, String author, boolean play) {
        this.parent = parent;
        this.title = title;
        this.author = author;
        this.play = play;

        shared = this;
        Intent intent = new Intent(this, NotificationPanel.class);
        intent.putExtra("methodName","cancel");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(parent, 0, intent, 0);
        nBuilder = new NotificationCompat.Builder(parent, "media_notification")
                .setSmallIcon(R.drawable.ic_stat_music_note)
                .setPriority(Notification.STREAM_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(new long[]{0L})
                .setDeleteIntent(pendingIntent)
                .setSound(null);

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

        // Setup listener
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                closeNotificationIfNotRunning();
            }
        }, 50, 950);


        // Listen for killed
        appFinishedExecutingListener.execute(this);
        new Thread() {
            @Override
            public void run() {
                try {
                    appFinishedExecutingListener.get();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            notificationCancel();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("methodName").equals("cancel")){
            notificationCancel();
        }
    }

    public void setListeners(RemoteViews view){
        // Пауза/Воспроизведение
        Intent intent = new Intent(parent, NotificationReturnSlot.class)
            .setAction("toggle")
            .putExtra("title", this.title)
            .putExtra("author", this.author)
            .putExtra("action", !this.play ? "play" : "pause");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(parent, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.toggle, pendingIntent);

        // Вперед
        Intent nextIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("next");
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(parent, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.next, pendingNextIntent);

        // Назад
        Intent prevIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("prev");
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(parent, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.prev, pendingPrevIntent);

        // Нажатие на уведомление
        Intent selectIntent = new Intent(parent, NotificationReturnSlot.class)
                .setAction("select");
        PendingIntent selectPendingIntent = PendingIntent.getBroadcast(parent, 0, selectIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        view.setOnClickPendingIntent(R.id.layout, selectPendingIntent);
    }

    static public void cancel() {
        if (NotificationPanel.shared != null) {
            NotificationPanel.shared.notificationCancel();
        }
    }

    public void notificationCancel() {
        nManager.cancel(NOTIFICATION_ID);
        shared = null;
    }

    public void closeNotificationIfNotRunning() {
        boolean running = isAppRunning(parent);
        if (!running) {
            // Remove the notification
            notificationCancel();
        }
    }

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


}

