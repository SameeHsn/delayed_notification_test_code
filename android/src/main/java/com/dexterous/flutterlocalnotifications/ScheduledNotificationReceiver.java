package com.dexterous.flutterlocalnotifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.core.app.NotificationManagerCompat;

import com.dexterous.flutterlocalnotifications.models.NotificationDetails;
import com.dexterous.flutterlocalnotifications.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import android.os.Bundle;
import java.lang.Exception;

import io.sentry.SentryLevel;
import io.sentry.android.core.SentryAndroid;
import android.app.Application;
import io.sentry.Sentry;

import java.text.ParseException;
import java.text.*;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.HashMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
/** Created by michaelbui on 24/3/18. */
@Keep
public class ScheduledNotificationReceiver extends BroadcastReceiver {

  private static final String TAG = "ScheduledNotifReceiver";
  private static final String SHARED_PREFERENCES_NAME = "FlutterSharedPreferences";
  private static final String FLUTTER_DELAYED_NNOTIFICATION_KEY = "flutter.FLUTTER_DELAYED_NOTIFICATION_KEY";
  private static SharedPreferences preferences;
  @Override
  @SuppressWarnings("deprecation")
  public void onReceive(final Context context, Intent intent) {
    //makin Obj for sharedpreferennce
    preferences=context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    
    String notificationDetailsJson =
        intent.getStringExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_DETAILS);
    if (StringUtils.isNullOrEmpty(notificationDetailsJson)) {
      // This logic is needed for apps that used the plugin prior to 0.3.4

      Notification notification;
      int notificationId = intent.getIntExtra("notification_id", 0);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        notification = intent.getParcelableExtra("notification", Notification.class);
      } else {
        notification = intent.getParcelableExtra("notification");
      }

      if (notification == null) {
        // This means the notification is corrupt
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
        Log.e(TAG, "Failed to parse a notification from  Intent. ID: " + notificationId);
        return;
      }

      notification.when = System.currentTimeMillis();
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(notificationId, notification);
      boolean repeat = intent.getBooleanExtra("repeat", false);
      if (!repeat) {
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
      }
    } else {
      Gson gson = FlutterLocalNotificationsPlugin.buildGson();
      Type type = new TypeToken<NotificationDetails>() {}.getType();
      NotificationDetails notificationDetails = gson.fromJson(notificationDetailsJson, type);

      FlutterLocalNotificationsPlugin.showNotification(context, notificationDetails);
      FlutterLocalNotificationsPlugin.scheduleNextNotification(context, notificationDetails);


      String isPowerSavingModeOn="";
      String isDoNotDisturbOn="";
      String isBatteryOptimizationEnabled="";
      
      Date date = new Date();
      SimpleDateFormat dashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      
      String formattedCurrentDateTime = dashDateTimeFormat.format(date);

        long epochMilli = ZonedDateTime.of(
                LocalDateTime.parse(notificationDetails.scheduledDateTime),
                        ZoneId.of(notificationDetails.timeZoneName)).toInstant().toEpochMilli();
        Instant instant = Instant.ofEpochMilli(epochMilli);
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime localDateTimeOfSchedualNotification = instant.atZone(zoneId).toLocalDateTime();

      String schedualTime=localDateTimeOfSchedualNotification.toString();
      String formatedSchedualDateTime=schedualTime.split("T")[0]+" "+ schedualTime.split("T")[1]+":00";
      Date cTime = new Date();
      Date sTime = new Date();


      try{
        cTime=dashDateTimeFormat.parse(formattedCurrentDateTime);
        sTime=dashDateTimeFormat.parse(formatedSchedualDateTime);
      }
      catch (Exception e) {
        Log.e("ParseException",e.toString());
      }

       Log.e("formattedCurrentDateTime",String.valueOf(formattedCurrentDateTime));
        Log.e("formatedSchedualDateTime",String.valueOf(formatedSchedualDateTime));
        Log.e("cTime",String.valueOf(cTime));
        Log.e("sTime",String.valueOf(sTime));
        Log.e("schedualTime",String.valueOf(schedualTime));
      
      Instant instant1 = cTime.toInstant();
      Instant instant2 = sTime.toInstant();

      // Calculate the difference in milliseconds
      long millisecondsDifference = Duration.between(instant1, instant2).toMillis();
      long inSeconds=millisecondsDifference/1000;

 Log.e("millisecondsDifference",String.valueOf(millisecondsDifference));
      Log.e("inSeconds",String.valueOf(inSeconds));
      Log.e("inSeconds>20",String.valueOf(inSeconds>20));
//      Log.d("----millisecondsDifference:",String.valueOf(inSeconds));
//      int result = cTime.compareTo(sTime);
//      Log.d("----current date time:",String.valueOf(date));
//      Log.d("----dashDateTimeFormat:",String.valueOf(dashDateTimeFormat));
//      Log.d("----formattedCurrentDateTime current date time:",String.valueOf(formattedCurrentDateTime));
//      Log.d("----localDateTimeOfSchedualNotification:",String.valueOf(localDateTimeOfSchedualNotification));
//      Log.d("----localDateTimeOfSchedualNotification as  String:",String.valueOf(schedualTime));
//      Log.d("----formatedSchedualDateTime:",String.valueOf(formatedSchedualDateTime));
//      Log.d("----cTime as parse:",String.valueOf(cTime));
//      Log.d("----sTime as parse:",String.valueOf(sTime));
//      Log.d("----sTimeWith20SecondAdded added 20 Seconds:",String.valueOf(sTimeWith20SecondAdded));
//      Log.d("----result:",String.valueOf(result));

      if (isPowerSavingModeOn(context)) {
        Log.d("isPowerSavingModeOn?:", "True");
        isPowerSavingModeOn="True";
      } else {
        Log.d("isPowerSavingModeOn?:", "False");
        isPowerSavingModeOn="False";
      }
      if (isDoNotDisturbOn(context)) {
        Log.d("isDoNotDisturbOn?:", "True");
        isDoNotDisturbOn="True";
      } else {
        Log.d("isDoNotDisturbOn?:", "False");
        isDoNotDisturbOn="False";
      }
      if (isBatteryOptimizationEnabled(context)) {
        Log.d("isBatteryOptimizationEnabled?:", "True");
        isBatteryOptimizationEnabled="True";

      } else {
        Log.d("isBatteryOptimizationEnabled?:", "False");
        isBatteryOptimizationEnabled="False";
      }
      
      
        String baseString=  "currentDateTime: " + formattedCurrentDateTime.toString() +" ,scheduledDateTime: " + formatedSchedualDateTime + " ,isPowerSavingModeOn: " +isPowerSavingModeOn.toString() + " ,isDoNotDisturbOn: " +isDoNotDisturbOn.toString() +" ,isBatteryOptimizationEnabled: " + isBatteryOptimizationEnabled.toString() +" ,noitification_title: " + notificationDetails.title.toString();
      if (inSeconds>20) {//cTime.isAfter(sTimeWith20SecondAdded)
         Log.d("---------------result:","Delayed Notification");
         try {
           Log.d("baseString:",baseString);
           HashMap<String, String> saveValue = new HashMap<String, String>();
           saveValue.put("currentDateTime",formattedCurrentDateTime.toString());
           saveValue.put("scheduledDateTime",formatedSchedualDateTime);
           saveValue.put("isPowerSavingModeOn",isPowerSavingModeOn.toString());
           saveValue.put("isDoNotDisturbOn",isDoNotDisturbOn.toString());
           saveValue.put("isBatteryOptimizationEnabled",isBatteryOptimizationEnabled.toString());

           String hashMapString = gson.toJson(saveValue);

           storePref(context,FLUTTER_DELAYED_NNOTIFICATION_KEY,hashMapString);
           throw new Exception(baseString);
         } catch (Exception e) {
           Sentry.captureException(e);
         }
       }
      else{
        Log.d("---------------result:","Not Delayed Notification");    
      }
    }
  }
  public boolean isPowerSavingModeOn(Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return powerManager != null && powerManager.isPowerSaveMode();
  }
  public static boolean isDoNotDisturbOn(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (notificationManager != null) {
        int currentInterruptionFilter = notificationManager.getCurrentInterruptionFilter();
        return currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY;
      }
    }
    return false;
  }

  public static boolean isBatteryOptimizationEnabled(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = context.getPackageName();
      PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      if (powerManager != null) {
        return !powerManager.isIgnoringBatteryOptimizations(packageName);
      }
    }
    return false;
  }

  //sharedpreference method
  public static void storePref(Context context,String key, String value) {
    preferences.edit().putString(key, value).commit();
  }
  
  public static void getPref(Context context, String key) {
    String result=preferences.getString(key,"");
     Log.d(" resulut is:", result);
  }
  
  public static void getKeys(Context context) {
    Log.d("-----getKeys:",preferences.getAll().toString());
  }
  
}
