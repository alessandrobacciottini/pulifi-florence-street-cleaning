package org.andreanencio.fineavoid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import java.util.concurrent.Callable;

public final class Utils {

    private static Callable<Void> function = null;

    private Utils() {
        throw new RuntimeException();
    }

    public static void alert(Activity context, String message, String title) {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(context);

        builder.setTitle(title)
                .setIcon(R.drawable.sweep)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    public static void alert(Context context, String message, String title, Callable<Void> functionYes) {

        function = functionYes;

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(context);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        doSomething();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private static void doSomething() {
        try {
            if (function != null)
                function.call();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
