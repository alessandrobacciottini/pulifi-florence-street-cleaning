package org.andreanencio.fineavoid;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {
    public ResultReceiver mReceiver;

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;

        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }

    public FetchAddressIntentService() {
        super("Geocoder");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ex) {
            errorMessage = "Errore " + ex.getMessage();
        } catch (IllegalArgumentException ex) {
            errorMessage = "Errore " + ex.getMessage();
        }
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty())
                errorMessage = "Nessun risultato";
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                addressFragments.add(address.getAddressLine(i));

            deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
