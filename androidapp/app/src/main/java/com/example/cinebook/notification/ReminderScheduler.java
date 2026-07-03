package com.example.cinebook.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Koristi AlarmManager da zakaze notifikaciju - podsetnik korisniku da mu film
 * pocinje na dan prikazivanja (screeningDate). Alarm se okida u 9h ujutru na taj dan.
 */
public class ReminderScheduler {

    public static void scheduleReminder(Context context, long reservationId, String movieTitle, String screeningDateYmd) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(screeningDateYmd));
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            // Ako je datum prikazivanja vec prosao, ne zakazujemo alarm
            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                return;
            }

            Intent intent = new Intent(context, ReservationReminderReceiver.class);
            intent.putExtra(ReservationReminderReceiver.EXTRA_MOVIE_TITLE, movieTitle);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) reservationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, long reservationId) {
        Intent intent = new Intent(context, ReservationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reservationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
