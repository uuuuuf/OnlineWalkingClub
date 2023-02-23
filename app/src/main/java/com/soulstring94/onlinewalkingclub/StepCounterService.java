package com.soulstring94.onlinewalkingclub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StepCounterService extends Service  implements SensorEventListener {

    // StepSensor=================================================
    private SensorManager sensorManager;
    private Sensor sensor;

    private long myTime1, myTime2;
    private float x, y, z;
    private float lastX, lastY, lastZ;
    private final int walkThreshold = 455;
    private double acceleration = 0;
    private int walkingCount = 0;

    private LocationManager locationManager;
    private LocationListener locationListener;

    boolean gpsFlag = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(1, notification);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        gpsFlag = true;
        startLocationUpdates();

        return START_STICKY;
    }

    private Notification createNotification() {
        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("StepCounterChannel", "Step Counter Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "StepCounterChannel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Step Counter Service")
                .setContentText("Counting steps...")
                .setOngoing(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT  | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        Intent stopIntent = new Intent(this, StepCounterService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        return builder.build();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            myTime2 = System.currentTimeMillis();
            long gab = myTime2 - myTime1;

            if (gab > 90) {
                myTime1 = myTime2;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                acceleration = Math.abs(x + y + z - lastX - lastY - lastZ) / gab * 9000;

                if (acceleration > walkThreshold) {
                    walkingCount++;

                    Intent intent = new Intent("com.soulstring94.stepcounter.STEP_COUNT");
                    intent.putExtra("count", walkingCount);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }

        // Update the step count in the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, createNotificationBuilder(walkingCount).build());
    }

    private NotificationCompat.Builder createNotificationBuilder(int stepCount) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "StepCounterChannel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Step Counter Service")
                .setContentText("Counting steps: " + stepCount)
                .setAutoCancel(true)
                .setOngoing(true);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT  | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // Add a stop button to the notification
        Intent stopIntent = new Intent(this, StepCounterService.class);
        stopIntent.setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop, "Stop", stopPendingIntent);

        return builder;
    }

    private void startLocationUpdates() {
        // LocationManager 객체 생성
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // LocationListener 객체 생성
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(gpsFlag) {
                    // 위치가 변경될 때마다 호출되는 메서드
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Intent intent = new Intent("com.soulstring94.stepcounter.GPS_INFO");
                    intent.putExtra("latlng", latitude + "," + longitude);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // 위치 공급자의 상태가 변경될 때 호출되는 메서드
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 위치 공급자가 사용 가능해질 때 호출되는 메서드
            }

            @Override
            public void onProviderDisabled(String provider) {
                // 위치 공급자가 사용 불가능해질 때 호출되는 메서드
            }
        };

        // GPS와 네트워크를 사용할 수 있는지 확인
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // 위치 업데이트 요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsFlag = false;
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}