package com.example.ae.data;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.ae.R;
import com.example.ae.TasksActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService
        extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {

        Log.d(
                "FCM_TOKEN",
                token
        );

        // Aquí después lo guardaremos en Firestore
    }



    @Override
    public void onMessageReceived(
            RemoteMessage remoteMessage
    ) {

        Intent intent =
                new Intent(
                        this,
                        TasksActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        String title =
                remoteMessage
                        .getData()
                        .get("title");

        String body =
                remoteMessage
                        .getData()
                        .get("body");

        body = body
                .replace(
                        " agregó: ",
                        " agregó una nueva tarea:\n\n"
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        "AE_CHANNEL_V3"
                )
                        .setSmallIcon(
                                R.mipmap.ic_launchere_round
                        )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(body)
                        )
                        .setContentIntent(
                                pendingIntent
                        )
                        .setAutoCancel(true)
                        .setPriority(
                                NotificationCompat.PRIORITY_MAX
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_SOCIAL
                        )
                        .setVisibility(
                                NotificationCompat.VISIBILITY_PUBLIC
                        )
                        .setVibrate(
                                new long[]{
                                        0,
                                        300,
                                        200,
                                        300
                                }
                        )
                        .setSubText(
                                "A&E ❤️"
                        )
                        .setTicker(
                                title
                        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat
                .from(this)
                .notify(
                        (int) System.currentTimeMillis(),
                        builder.build()
                );
    }
}