package com.SyndicG5.ui.ContainerHome.fragments.service;

import static com.SyndicG5.ui.util.NotificationActionReceiver.NOTIFICATION_GROUP_REQUEST_CODE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.SyndicG5.R;
import com.SyndicG5.ui.util.NotificationHelper;

import timber.log.Timber;

public class MapService extends Service {

    public static final int RESTART_SERVICE_REQUEST_CODE = 107;
    boolean foundArea=false;

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(MapService.this,"bind",Toast.LENGTH_SHORT).show();
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.i("Service onStartCommand");
        foundArea=false;

//        if(auth.getCurrentUser()!=null){
//            db.getReference().child("BookedSlots")
//                    .addChildEventListener(new ChildEventListener() {
//                        @RequiresApi(api = Build.VERSION_CODES.M)
//                        @Override
//                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
//                            updateBookedSlots(snapshot,bookedSlots);
//                            notificationUpdate();
//                        }
//                        @RequiresApi(api = Build.VERSION_CODES.M)
//                        @Override
//                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            BookedSlots bookedSlots = snapshot.getValue(BookedSlots.class);
//                            updateBookedSlots(snapshot,bookedSlots);
//                            notificationUpdate();
//                        }
//                        @Override
//                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
//
//                        @Override
//                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {}
//                    });
//        }
        return START_STICKY;
    }

//
//    private void updateBookedSlots(DataSnapshot snapshot,BookedSlots bookedSlots) {
//        if(auth.getCurrentUser()!=null){
//            if((bookedSlots.readNotification==0 && bookedSlots.checkout==0 && bookedSlots.hasPaid==1) && bookedSlots.userID.equals(auth.getCurrentUser().getUid())) {
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(bookedSlots.endTime);
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//
//                Log.i(String.valueOf(this.getClass()), snapshot.getKey() + " onChildChanged ,id " + Math.abs(bookedSlots.notificationID) + ", Success: Alarm at " + simpleDateFormat.format(calendar.getTime()));
//
//                if (calendar.before(Calendar.getInstance()))
//                    Log.i(String.valueOf(this.getClass()),"Old Notification!");
//                else {
//                    Log.i(String.valueOf(this.getClass()),"New Notification!");
//                }
//
//                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
//                intent.putExtra("title", snapshot.getKey());
//                intent.putExtra("message", "Check-out your Booking");
//                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
//                intent.putExtra("readID", snapshot.getKey());
//                intent.putExtra("when", "later");
//                calendar.add(Calendar.MINUTE, -5); //subtract 5 minutes
//                AlarmUtils.addAlarm(getApplicationContext(),
//                        intent,
//                        Math.abs(bookedSlots.notificationID)-1,
//                        calendar);
//                Log.i("NotificationTrigger","Add user checkout alarm");
//            }
//            if(bookedSlots.readBookedNotification==0 && bookedSlots.checkout==0 && bookedSlots.userID.equals(auth.getCurrentUser().getUid())){
//                Calendar calendar = Calendar.getInstance();
//                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
//                intent.putExtra("title", snapshot.getKey());
//                intent.putExtra("message", "Confirm your Booking");
//                intent.putExtra("notificationID", Math.abs(bookedSlots.notificationID));
//                intent.putExtra("readID", snapshot.getKey());
//                intent.putExtra("when", "now");
//                AlarmUtils.addAlarm(getApplicationContext(),
//                        intent,
//                        Math.abs(bookedSlots.notificationID)-1,
//                        calendar);
////                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
////                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), Math.abs(bookedSlots.notificationID)-1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
////                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
////                } else {
////                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
////                }
//                Log.i("NotificationTrigger","Add user confirm alarm");
//            }
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void notificationUpdate() {
        NotificationHelper notificationHelper=new NotificationHelper(getApplicationContext());
        int notificationCount=notificationHelper.countNotificationGroup(getApplicationContext().getString(R.string.notification_group_id_1));
        if(notificationCount<=1){
            notificationHelper.cancelNotification(NOTIFICATION_GROUP_REQUEST_CODE);
        }
        Log.i(String.valueOf(this.getClass()),"Notifications Count: ".concat(String.valueOf(notificationCount)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("Service onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Timber.i("Service onTaskRemoved");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), RESTART_SERVICE_REQUEST_CODE
                , restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);
    }
}
