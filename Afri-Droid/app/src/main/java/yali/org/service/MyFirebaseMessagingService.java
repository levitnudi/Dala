package yali.org.service;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;

import yali.org.R;
import yali.org.activity.HomeActivity;
import yali.org.view.NotificationUtils;
import yali.org.view.NotificationVO;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgingService";
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String IMAGE = "image";
    private static final String ACTION = "action";
    private static final String ACTION_DESTINATION = "action_destination";
    public static String REQUEST_ACCEPTED = "yali.org.service.MyFirebaseMessagingService";
    private SharedPreferences shared;
    private SharedPreferences.Editor editor;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            handleData(data);
        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());

        }// Check if message contains a notification payload.

    }

    @Override
    public void onCreate(){
        super.onCreate();
        shared = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        editor = shared.edit();
    }

    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);

        editor.putString("message", message);
        editor.commit();

        Uri rateLink = Uri.parse("market://details?id=" + getPackageName());
        Uri rateLinkNotFound = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, rateLink);
        Intent rateNotFound = new Intent(Intent.ACTION_VIEW, rateLinkNotFound);
        try{
            startActivity(rateIntent);
        }catch (ActivityNotFoundException e){
            startActivity(rateNotFound);
        }

        Intent resultIntent = rateIntent;//new Intent(getApplicationContext(), ScrollingActivity.class);
        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);
        notificationUtils.playNotificationSound();
    }

    private void handleData(Map<String, String> data) {
        String title = data.get(TITLE);
        String message = data.get(MESSAGE);
        String iconUrl = data.get(IMAGE);
        String action = data.get(ACTION);
        String actionDestination = data.get(ACTION_DESTINATION);
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);
        notificationVO.setIconUrl(iconUrl);
        notificationVO.setAction(action);
        notificationVO.setActionDestination(actionDestination);

        Intent resultIntent = new Intent(getApplicationContext(), HomeActivity.class);

        editor.putString("message", message);
        editor.commit();

        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);
        notificationUtils.playNotificationSound();

    }

}
