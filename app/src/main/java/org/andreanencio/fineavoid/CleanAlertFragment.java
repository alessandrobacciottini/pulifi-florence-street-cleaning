package org.andreanencio.fineavoid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CleanAlertFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        String clean = args.getString("clean");

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.sweep)
                .setTitle("Pulizie")
                .setMessage(clean)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }
}