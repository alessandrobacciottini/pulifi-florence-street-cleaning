package org.andreanencio.fineavoid;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import java.text.ParseException;
import java.util.Calendar;

public class AlertParking extends DialogFragment {

    public static MainActivity activity = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle arg = getArguments();
        String response = arg.getString("response");
        final String orarioPulizie = response.substring(response.lastIndexOf(":") + 1);
        String[] parts = orarioPulizie.split("-");
        String before = parts[0];
        String before2 = before.replaceAll("\\s", "");
        String after = parts[1];
        String[] parts2 = before2.split("[.]");

        final String oras = parts2[0];
        final String minutis = parts2[1];
        final int oraInizio = Integer.parseInt(oras);
        final int minutiInizio = Integer.parseInt(minutis);

        return new AlertDialog.Builder(getActivity())

                .setIcon(R.drawable.car)
                .setTitle("Parcheggio")
                .setMessage("Hai parcheggiato qui! Vuoi ricevere notifiche?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
    }

    public static void scheduleNotification(Context context, Notification notification, int oraInizio, int minutiInizio) throws ParseException {

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, oraInizio);
        cal.set(Calendar.MINUTE, minutiInizio);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Notifica");

        String minuti;
        if (minutiInizio == 0)
            minuti = "00";
        else
            minuti = Integer.toString(minutiInizio);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setMessage("Hai impostato la notifica per le " + oraInizio + ":" + minuti + ". Per vederla/eliminarla tocca l'icona sulla mappa");
        AlertDialog dialog = builder.create();
        dialog.setIcon(R.drawable.bell);
        dialog.show();
    }

    public static Notification getNotification(Context context, String content) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Pulizia imminente!");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.car);
        builder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        return builder.build();
    }
}
