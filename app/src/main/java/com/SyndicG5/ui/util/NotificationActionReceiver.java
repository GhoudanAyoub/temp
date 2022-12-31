package com.SyndicG5.ui.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.SyndicG5.R;

import java.util.Objects;

import timber.log.Timber;

public class NotificationActionReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_GROUP_REQUEST_CODE = 108;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void performMarkAsReadCheckout(Context context, Intent intent){
        NotificationHelper notificationHelper=new NotificationHelper(context);
        Timber.e(Objects.requireNonNull(intent.getStringExtra("readID")));
        notificationHelper.cancelNotification(intent.getIntExtra("notificationID",1));
        int notificationCount=notificationHelper.countNotificationGroup(context.getString(R.string.notification_group_id_1));
        if(notificationCount<=1){
            notificationHelper.cancelNotification(NOTIFICATION_GROUP_REQUEST_CODE);
        }
        Timber.i("Notifications Count: ".concat(String.valueOf(notificationCount)));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void performMarkAsReadBooking(Context context, Intent intent){
        NotificationHelper notificationHelper=new NotificationHelper(context);
        Log.e("MarkAsReadBooking", Objects.requireNonNull(intent.getStringExtra("readID")));
        notificationHelper.cancelNotification(intent.getIntExtra("notificationID",1));
        int notificationCount=notificationHelper.countNotificationGroup(context.getString(R.string.notification_group_id_1));
        if(notificationCount<=1){
            notificationHelper.cancelNotification(NOTIFICATION_GROUP_REQUEST_CODE);
        }
        Log.i(String.valueOf(this.getClass()),"Notifications Count: ".concat(String.valueOf(notificationCount)));
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getStringExtra("action");
        if(action.equals("MarkAsReadCheckout")){
            performMarkAsReadCheckout(context, intent);
        }else if(action.equals("MarkAsReadBooking")){
            performMarkAsReadBooking(context, intent);
        }
    }
}
