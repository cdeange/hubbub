package com.deange.githubstatus.controller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.deange.githubstatus.R;
import com.deange.githubstatus.model.Message;
import com.deange.githubstatus.model.State;
import com.deange.githubstatus.net.FirebaseService;
import com.deange.githubstatus.ui.MainActivity;
import com.deange.githubstatus.util.RxBroadcastReceiver;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.deange.githubstatus.net.FirebaseService.ACTION_MESSAGE_RECEIVED;

@Singleton
public class NotificationController {

    private static final String TAG = "NotificationController";
    private static final int NOTIFICATION_ID = 0x420;

    private final Object mLock = new Object();
    private final Context mContext;
    private final NotificationManager mManager;
    private volatile boolean mRegistered;

    @Inject
    public NotificationController(final Context context) {
        mContext = context;
        mManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public void register() {
        if (!mRegistered) {
            synchronized (mLock) {
                if (!mRegistered) {
                    doRegister();
                    mRegistered = true;
                }
            }
        }
    }

    private void doRegister() {
        ensureNotificationChannels();

        // Listen for incoming status message updates
        RxBroadcastReceiver.create(mContext, new IntentFilter(ACTION_MESSAGE_RECEIVED))
                           .map(FirebaseService::getMessageFromIntent)
                           .subscribe(this::showNotification);
    }

    public void showNotification(final Message message) {
        final State state = message.state();

        final String channelId = state.name();

        final String title = mContext.getString(R.string.app_name);
        final String description = message.bodyForNotification(mContext);
        final int icon = R.drawable.ic_notification_git;
        final int color = ContextCompat.getColor(mContext, state.getColorResId());

        final NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, channelId);
        builder.setContentTitle(title);
        builder.setContentText(description);
        builder.setSmallIcon(icon);
        builder.setColor(color);
        builder.setStyle(new NotificationCompat.BigTextStyle());

        builder.setAutoCancel(true);
        builder.setContentIntent(PendingIntent.getActivity(
                mContext,
                NOTIFICATION_ID,
                new Intent(mContext, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT));

        mManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void ensureNotificationChannels() {
        if (!supportsNotificationChannels()) {
            return;
        }

        final Map<String, NotificationChannel> existingChannels =
                mManager.getNotificationChannels()
                        .stream()
                        .collect(Collectors.toMap(
                                NotificationChannel::getId,
                                channel -> channel));

        for (final State state : State.values()) {
            final String id = state.name();
            if (existingChannels.containsKey(id)) {
                existingChannels.remove(id);
                continue;
            }

            final String name = mContext.getString(state.getTitleResId());
            final String description = mContext.getString(state.getDescriptionResId());
            final int color = ContextCompat.getColor(mContext, state.getColorResId());
            final int importance = NotificationManager.IMPORTANCE_DEFAULT;

            final NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(color);

            Log.d(TAG, "Creating notification channel: " + id);
            mManager.createNotificationChannel(channel);
        }

        // Leftover channels (maybe from an update?)
        for (final String staleChannelId : existingChannels.keySet()) {
            Log.d(TAG, "Deleting notification channel: " + staleChannelId);
            mManager.deleteNotificationChannel(staleChannelId);
        }

        existingChannels.clear();
    }

    private boolean supportsNotificationChannels() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

}