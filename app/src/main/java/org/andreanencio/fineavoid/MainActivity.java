package org.andreanencio.fineavoid;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String P_CLICKED = "p1";
    public static final String P_ENABLED = "p2";
    public int GPS_PERMISSION = 0;
    public String lastNotifData;

    public SharedPreferences settings;
    public MapFragment mapFragment;

    public android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
    public LocationListener locationListener;
    public LocationManager locationManager;

    public Context context;
    public Activity activity;
    public MainActivity me;

    private FloatingActionButton notifButton;
    private FloatingActionButton park;
    private FloatingActionButton locateButton;
    private Button hiddenLocateButton;
    private EditText searchField;
    private TextView streetField;

    private boolean isGPSAllowed;
    private boolean isParked = false;
    private boolean isNotificationActive = false;
    private boolean isSearchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        activity = this;
        me = this;

        NotificationPublisher.activity = me;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        streetField = (TextView)findViewById(R.id.streetView) ;
        streetField.setTextColor(Color.WHITE);
        streetField.setBackgroundColor(Color.parseColor("#434a9d"));

        locateButton = (FloatingActionButton)findViewById(R.id.locateButton);
        park = (FloatingActionButton) findViewById(R.id.parkButton);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        View root = findViewById(R.id.cleanButton).getRootView();
        root.setBackgroundColor(getResources().getColor(R.color.custom));

        isGPSAllowed = settings.getBoolean("use_gps", true);
        if(!isGPSAllowed)
            Utils.alert(this, "Ti consigliamo di attivare il GPS nelle impostazioni per un utilizzo ottimale dell'applicazione.", "GPS disattivato");

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        mapFragment.activity = me;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                return;
            else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);

        Button searchButton = (Button)findViewById(R.id.searchButton);
        searchButton.setTextColor(Color.WHITE);
        searchField = (EditText)findViewById(R.id.searchField);
        searchField.setBackgroundColor(Color.WHITE);
        searchField.setTextColor(Color.BLACK);
        searchField.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isSearchVisible) {
                    searchField.setVisibility(View.VISIBLE);
                    isSearchVisible = true;
                    char[] prompt = {'V', 'i', 'a', ' '};
                    searchField.setText(prompt, 0, prompt.length);
                    searchField.setSelection(searchField.length());
                    searchField.requestFocus();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchField, InputMethodManager.SHOW_IMPLICIT);
                } else {

                    View view_ = me.getCurrentFocus();
                    if (view_ != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view_.getWindowToken(), 0);
                    }

                    isSearchVisible = false;
                    searchField.setVisibility(View.INVISIBLE);
                    String text = searchField.getText().toString();

                    if (!text.equals("Via ") && !text.equals("Via") && !text.equals("") && !text.equals(" ")) {
                        text = text + " Firenze";
                        Geocoder geocoder = new Geocoder(getBaseContext());
                        List<Address> addrs = null;

                        try {
                            addrs = geocoder.getFromLocationName(text, 1);
                            if (addrs != null && !addrs.equals(""))
                                mapFragment.setCoordinates(new LatLng(addrs.get(0).getLatitude(),
                                        addrs.get(0).getLongitude()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey("P_ENABLED")) {
            park.setClickable(savedInstanceState.getBoolean("P_CLICKED"));
            park.setEnabled(savedInstanceState.getBoolean("P_ENABLED"));
        } else {
            park.setClickable(false);
            park.setEnabled(false);
        }

        park.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isParked) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION)) {

                        } else
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
                    }
                    Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (l == null) {
                        AlertDialog.Builder alertNoPosition = new AlertDialog.Builder(context);
                        alertNoPosition.setTitle("Posizione non trovata!").show();
                    } else {
                        Geocoder geoCoder = new Geocoder(context);
                        List<Address> matches = null;
                        try {
                            matches = geoCoder.getFromLocation(mapFragment.location.latitude, mapFragment.location.longitude, 1);
                        } catch (Exception ex) {
                            Utils.alert(context, ex.getMessage(), "Errore");
                        }

                        Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
                        if(bestMatch != null)
                        {
                            streetField.setText(bestMatch.getThoroughfare().toCharArray(), 0, bestMatch.getThoroughfare().length());
                            String response = CleanParser.parse(context, bestMatch.getThoroughfare());

                            if(response != null)
                            {
                                isParked = true;
                                mapFragment.setParkCoordinates(mapFragment.location);

                                String orarioPulizie = response.substring(response.lastIndexOf(":") + 1);
                                String[] parts = orarioPulizie.split("-");
                                String before = parts[0];
                                String before2 = before.replaceAll("\\s", "");
                                String[] parts2 = before2.split("[.]");
                                String oras = parts2[0];
                                String minutis = parts2[1];

                                lastNotifData = response;

                                int oraInizio = Integer.parseInt(oras);
                                int minutiInizio = Integer.parseInt(minutis);
                                try {
                                    String content = "Pulizia strada alle ore " + oras + ":" + minutis + ". Rimuovi la macchina!";
                                    AlertParking.scheduleNotification(context, AlertParking.getNotification(context, content), oraInizio, minutiInizio);
                                    me.changeNotificationStatus(true);
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            else
                            {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setIcon(R.drawable.bell);
                                builder.setTitle("Ooops");
                                builder.setMessage("Questa strada sembra non appartenere al comune di Firenze.\n\nImpossibile attivare la notifica.");
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                                builder.show();
                            }
                        }
                    }
                }
            }
        });

        hiddenLocateButton = (Button) findViewById(R.id.hiddenLocateButton);
        hiddenLocateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    } else
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSION);
                }

                boolean gpsLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (gpsLocationEnabled) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
                    Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (l == null) {
                        AlertDialog.Builder alertNoPosition = new AlertDialog.Builder(context);
                        alertNoPosition.setTitle("Posizione non identificata").show();
                    } else {

                        Geocoder geoCoder = new Geocoder(context);
                        List<Address> matches = null;
                        try {
                            matches = geoCoder.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
                        } catch (Exception ex) {ex.printStackTrace();}

                        if (matches != null) {
                            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
                            streetField.setText(bestMatch.getThoroughfare().toCharArray(), 0, bestMatch.getThoroughfare().length());
                        }

                        mapFragment.setCoordinates(new LatLng(l.getLatitude(), l.getLongitude()));
                        park.setEnabled(true);
                        park.setClickable(true);
                    }
                } else {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                    else
                        builder = new AlertDialog.Builder(context);

                    builder.setTitle("GPS disattivato")
                            .setMessage("Vuoi concedere l'auorizzazione all'uso del GPS?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent1);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }

        });

        Button cleanButton = (Button) findViewById(R.id.cleanButton);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Geocoder geoCoder = new Geocoder(context);
                List<Address> matches = null;
                try {
                    matches = geoCoder.getFromLocation(mapFragment.location.latitude, mapFragment.location.longitude, 1);
                } catch (Exception ex) {
                    Utils.alert(context, ex.getMessage(), "Errore");
                }

                if (matches != null) {
                    Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
                    streetField.setText(bestMatch.getThoroughfare().toCharArray(), 0, bestMatch.getThoroughfare().length());

                    String response = CleanParser.parse(context, bestMatch.getThoroughfare());
                    if (response != null) {
                        CleanAlertFragment alertdFragment = new CleanAlertFragment();
                        Bundle args = new Bundle();
                        args.putString("clean", response);
                        alertdFragment.setArguments(args);
                        alertdFragment.show(fragmentManager, "Alert Dialog Fragment");
                    }
                }
            }
        });

        notifButton = (FloatingActionButton) findViewById(R.id.notifButton);
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setIcon(R.drawable.bell)
                            .setTitle("Notifica attiva")
                            .setMessage(lastNotifData + "\n\nVuoi disattivarla?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    changeNotificationStatus(false);
                                    removeNotificationAndParking();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                } catch (Exception ex) {
                    Utils.alert(context, ex.getMessage(), "Errore");
                }
            }
        });

        locateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hiddenLocateButton.performClick();
            }
        });

        changeNotificationStatus(isNotificationActive);
        hiddenLocateButton.performClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        super.onSaveInstanceState(savedInstanceState);
        FloatingActionButton park = (FloatingActionButton) findViewById(R.id.parkButton);

        boolean parkButtonClicked;
        boolean parkButtonEnabled;
        if (park.isEnabled()) {
            parkButtonClicked = true;
            parkButtonEnabled = true;
        } else {
            parkButtonClicked = false;
            parkButtonEnabled = false;
        }
        savedInstanceState.putBoolean(P_ENABLED, parkButtonClicked);
        savedInstanceState.putBoolean(P_CLICKED, parkButtonEnabled);
    }

    public void changeNotificationStatus(boolean status)
    {
        isNotificationActive = status;
        notifButton.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        park.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
    }

    public void removeNotificationAndParking() {
        isParked = false;
        park.setBackgroundColor(Color.BLUE);

        ArrayList<Marker> toBeRemoved = new ArrayList<Marker>();
        for (Marker m : mapFragment.markers) {
            if (m.getTitle().equals("La tua macchina"))
                toBeRemoved.add(m);
        }

        for (Marker m : toBeRemoved) {
            mapFragment.markers.remove(m);
            m.remove();
        }

        changeNotificationStatus(false);
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        hiddenLocateButton.performClick();
    }

    public void reverseGeocode() {
        Geocoder geoCoder = new Geocoder(context);
        List<Address> matches = null;
        try {
            matches = geoCoder.getFromLocation(mapFragment.location.latitude, mapFragment.location.longitude, 1);
        } catch (Exception ex) {
            Utils.alert(context, ex.getMessage(), "Errore");
        }

        if (matches != null) {
            Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
            streetField.setText(bestMatch.getThoroughfare().toCharArray(), 0, bestMatch.getThoroughfare().length());
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
