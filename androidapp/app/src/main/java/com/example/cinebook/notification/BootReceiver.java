package com.example.cinebook.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Nakon restarta uredjaja, AlarmManager alarmi se brisu - u punoj implementaciji bi ovde
 * ucitali listu rezervacija (npr. iz lokalne baze) i ponovo zakazali podsetnike pozivom
 * ReminderScheduler.scheduleReminder(...) za svaku aktivnu rezervaciju.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // TODO: ucitati rezervacije sa /reservations/my i ponovo zakazati alarme
        }
    }
}
