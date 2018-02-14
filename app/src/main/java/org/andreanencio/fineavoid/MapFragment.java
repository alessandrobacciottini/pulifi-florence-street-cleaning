package org.andreanencio.fineavoid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;
import java.util.ArrayList;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class MapFragment extends Fragment {
    public static MainActivity activity;
    public LatLng location;
    public LatLng parkLocation;
    public ArrayList<Marker> markers;

    private MapFragment me;
    private MapView mapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map, container, false);

        me = this;
        markers = new ArrayList<Marker>();
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        me.setCoordinates(latLng);
                    }
                });
            }
        });

        return rootView;
    }

    public void setCoordinates(LatLng position) {
        this.location = position;
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                Marker marker = googleMap.addMarker(new MarkerOptions().position(location).title(""));
                ArrayList<Marker> toBeRemoved = new ArrayList<Marker>();
                for (Marker m : markers) {
                    if (!m.getTitle().equals("La tua macchina")) {
                        toBeRemoved.add(m);
                    }
                }

                for (Marker m : toBeRemoved) {
                    markers.remove(m);
                    m.remove();
                }

                markers.add(marker);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 16);
                googleMap.animateCamera(cameraUpdate);

                activity.reverseGeocode();
            }
        });
    }

    public void setParkCoordinates(LatLng position) {
        this.parkLocation = position;
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(parkLocation).title("La tua macchina").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                markers.add(marker);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker arg0) {
                        if (arg0.getTitle().equals("La tua macchina")) {
                            String message;

                            message = activity.lastNotifData + "\n\nVuoi disattivarla?";

                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setIcon(R.drawable.bell);
                            builder.setTitle("Notifica attiva");
                            builder.setMessage(message);
                            builder.setPositiveButton("Sì", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    activity.changeNotificationStatus(false);
                                    activity.removeNotificationAndParking();
                                }
                            });
                            builder.setNegativeButton("No", null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                        return true;
                    }
                });

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(parkLocation, 16);
                googleMap.animateCamera(cameraUpdate);
            }
        });
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}