package com.sndo.fcm.demo4;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sndo.fcm.demo4.util.FcmUtil;
import com.sndo.fcm.demo4.util.LContext;
import com.sndo.fcm.demo4.util.ToastUtil;

import java.lang.reflect.Method;

/**
 * @author 张全
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static int ID = 1;

    /**
     * 监控令牌的生成
     * <p>
     * FCM SDK 会为客户端应用实例生成一个注册令牌。如果您希望指定单一目标设备或者创建设备组，则需要通过继承 FirebaseMessagingService 并重写 onNewToken 来获取此令牌。
     * 获取该令牌后，您可以将其发送到应用服务器，并使用您偏好的方法进行存储。
     *
     * @param token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FcmUtil.log("MyFirebaseMessagingService onNewToken, token=" + token);
        FcmUtil.saveToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show("onMessageReceived msg=" + remoteMessage);
            }
        });


        FcmUtil.log("==================RemoteMessage==============");
        Class<? extends RemoteMessage> aClass = remoteMessage.getClass();
        Method[] declaredMethods = aClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            method.setAccessible(true);
            if (method.getName().startsWith("get")) {
                Object value = null;
                try {
                    value = method.invoke(remoteMessage);
                    FcmUtil.log(method.getName() + "=" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        FcmUtil.log("==================RemoteMessage==============");

        // Check if message contains a data payload. 检查是否包含data
        if (remoteMessage.getData().size() > 0) {
            FcmUtil.log("MyFirebaseMessagingService  onMessageReceived, data: " + remoteMessage.getData());


            //do something

        }

        // Check if message contains a notification payload. 检查是否包含notification
        if (remoteMessage.getNotification() != null) {
            final String title = remoteMessage.getNotification().getTitle();
            final String body = remoteMessage.getNotification().getBody();
            FcmUtil.log("MyFirebaseMessagingService  onMessageReceived, Notification[title: " + title + ",body=" + body + "]");

            //发送notification
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendNotification(MyFirebaseMessagingService.this, title, body);
                }
            });
        }
    }


    public static void sendNotification(Context ctx, String title, String messageBody) {
        FcmUtil.log("发送通知 title=" + title + ",content=" + messageBody);
        if (TextUtils.isEmpty(messageBody)) return;
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = LContext.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(ctx, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) LContext.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android O notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    LContext.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(ID, notificationBuilder.build());
        ID++;
    }

//    public static void sendNotification(Context ctx, String content) {
//        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder mBuilder;
//        String channelId = "1";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId,
//                    "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.enableLights(false);
//            channel.enableVibration(false);
//            channel.setVibrationPattern(new long[]{0});
//            channel.setSound(null, null);
//            channel.setLightColor(Color.RED); //小红点颜色
//            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
//            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
//            mBuilder = new NotificationCompat.Builder(ctx, channelId);
//            notificationManager.createNotificationChannel(channel);
//        } else {
//            mBuilder = new NotificationCompat.Builder(ctx);
//            mBuilder.setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
//                    .setVibrate(new long[]{0})
//                    .setSound(null);
//        }
//
//        Intent intent = new Intent(ctx, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentTitle(LContext.getString(R.string.app_name))
//                .setContentText(content)
//                .setContentIntent(pendingIntent)
//                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setAutoCancel(true);
//
//        Notification notify = mBuilder.build();
//        notificationManager.notify(ID, notify);
//        ID++;
//    }
}
