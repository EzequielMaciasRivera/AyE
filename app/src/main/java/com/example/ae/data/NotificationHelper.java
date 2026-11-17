package com.example.ae.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.example.ae.R;

public class NotificationHelper {

    public static void createNotificationChannel(
            Context context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri soundUri =
                    Uri.parse(
                            "android.resource://"
                                    + context.getPackageName()
                                    + "/"
                                    + R.raw.guitar
                    );

            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes.USAGE_NOTIFICATION
                            )
                            .build();

            NotificationChannel channel =
                    new NotificationChannel(
                            "AE_CHANNEL_V3",
                            "A&E Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setSound(
                    soundUri,
                    audioAttributes
            );

            channel.enableVibration(true);

            channel.setVibrationPattern(
                    new long[]{
                            0,
                            300,
                            200,
                            300,
                            200,
                            500
                    }
            );


            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {
                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }
}