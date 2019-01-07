package com.example.medianotification;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;


/** MediaNotificationPlugin */
public class MediaNotificationPlugin implements MethodCallHandler {
    private static final String TAG = "MediaNotificationPanel";
    private static final String CHANNEL_ID = "media_notification";
    private static Registrar registrar;
    private static NotificationPanel nPanel;
    private static MethodChannel channel;

    private MediaNotificationPlugin(Registrar r) {
        registrar = r;
    }

    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
        MediaNotificationPlugin plugin = new MediaNotificationPlugin(registrar);

        MediaNotificationPlugin.channel = new MethodChannel(registrar.messenger(), "media_notification");
        MediaNotificationPlugin.channel.setMethodCallHandler(new MediaNotificationPlugin(registrar));
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "show":
                final String title = call.argument("title");
                final String author = call.argument("author");
                final boolean play = call.argument("play");
                final boolean beint = call.argument("beint");
                show(title, author, play, beint);
                result.success(null);
                break;

            case "hide":
                hide();
                result.success(null);
                break;

            case "stopSound":
                stopSound();
                result.success(null);
                break;

            case "getWifiLock":
                getWifiLock();
                result.success(null);
                break;

            case "releaseWifiLock":
                releaseLocks();
                result.success(null);
                break;

            default:
                result.notImplemented();
        }
    }

    public static void callEvent(String event) {
        if (channel != null) {
            MediaNotificationPlugin.channel.invokeMethod(event, null, new Result() {
                @Override
                public void success(Object o) { }

                @Override
                public void error(String s, String s1, Object o) { }

                @Override
                public void notImplemented() { }
            });
        } else {
            Log.e(TAG, "Message callEvent - Channel Null");
        }
    }

    public static void show(String title, String author, boolean play, boolean beint) {
        Log.i(TAG, "show");
        if (nPanel != null) {
            nPanel.releaseLocks();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            if (channel != null) {
                channel.enableVibration(false);
                channel.setSound(null, null);
                NotificationManager notificationManager = registrar.context().getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
        nPanel = new NotificationPanel(registrar.context(), title, author, play, beint);
    }

    private void hide() {
        Log.i(TAG, "hide");
        if (nPanel != null) {
            nPanel.notificationCancel();
        }
    }
    private void stopSound() {
        Log.i(TAG, "stopSound");
        if (nPanel != null) {
            nPanel.stopSound();
        } else {
            show("RÚV", "", false, false);
            nPanel.stopSound();
            hide();
        }
    }

    private void getWifiLock() {
        Log.i(TAG, "getWifiLock");
        if (nPanel != null) {
            nPanel.getWifiLock();
        } else {
            show("RÚV", "", false, false);
            nPanel.getWifiLock();
            hide();
        }

    }

    private void releaseLocks() {
        Log.i(TAG, "releaseWifiLock");
        if (nPanel != null) {
            nPanel.releaseLocks();
        } else {
            show("RÚV", "", false, false);
            nPanel.releaseLocks();
            hide();
        }
    }

    public void onDestroy() {
        hide();
    }
}
