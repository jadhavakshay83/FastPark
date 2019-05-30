package in.hatcube.fastpark;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public BottomSheetBehavior bottomSheetBehavior,ddbottomSheetBehavior;
    public BitmapDrawable bitFreePark,bitFullPark;
    public Bitmap parkingFree,parkingFull;
    public Button directions,directions1;
    boolean doubleBackToExitPressedOnce = false;
    public TextView slotTitle,slotAddress,slotContact;
    JSONObject selectedMarker;
    JSONArray markers;
    int position;
    ProgressDialog progress;
    public ImageView parkSpace1,parkSpace2,parkSpace3,parkSpace4,parkSpace5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkLocationPermission();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get the bottom sheet view
        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        LinearLayout ddBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet_dynamic);

        directions = findViewById(R.id.directions);
        directions1 = findViewById(R.id.directions_dynamic);
        slotTitle = findViewById(R.id.slotTitle);
        slotAddress = findViewById(R.id.slotAddress);
        slotContact = findViewById(R.id.slotContact);

        //get dynamic park spaces
        parkSpace1 = findViewById(R.id.parkSpace1);
        parkSpace2 = findViewById(R.id.parkSpace2);
        parkSpace3 = findViewById(R.id.parkSpace3);
        parkSpace4 = findViewById(R.id.parkSpace4);
        parkSpace5 = findViewById(R.id.parkSpace5);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        ddbottomSheetBehavior = BottomSheetBehavior.from(ddBottomSheet);

        int height = 100;
        int width = 100;
        bitFreePark=(BitmapDrawable)getResources().getDrawable(R.drawable.parking_free);
        Bitmap b=bitFreePark.getBitmap();
        parkingFree = Bitmap.createScaledBitmap(b, width, height, false);

        bitFullPark=(BitmapDrawable)getResources().getDrawable(R.drawable.parking_full);
        Bitmap b1=bitFullPark.getBitmap();
        parkingFull = Bitmap.createScaledBitmap(b1, width, height, false);

        directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    selectedMarker = markers.getJSONObject(position);
                    Double lat = selectedMarker.getDouble("lat");
                    Double lng = selectedMarker.getDouble("lng");

                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lng));
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        directions1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=19.1048679,74.7359692"));
                startActivity(intent);
            }
        });


        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
    }

    @Override
    public void onMapReady(GoogleMap map) {

        markers = getMarkers();

        try {
            for (int i = 0; i < markers.length(); i++) {
                JSONObject row = markers.getJSONObject(i);
                int id = row.getInt("id");
                String name = row.getString("name");
                String parking = row.getString("parking");
                Double lat = row.getDouble("lat");
                Double lng = row.getDouble("lng");

                if(parking.equals("free")) {
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromBitmap(parkingFree)))
                            .setTag(id);
                } else {
                    map.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title(name)
                            .icon(BitmapDescriptorFactory.fromBitmap(parkingFull)))
                            .setTag(id);
                }
            }
            //Add dynamic marker
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(19.1048679, 74.7359692))
                    .title("Tarakpur Bus Stand")
                    .icon(BitmapDescriptorFactory.fromBitmap(parkingFree)))
                    .setTag(5);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng coordinate = new LatLng(latitude, longitude);
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 12);
                map.animateCamera(yourLocation);
            }
        }

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                position = (int) (marker.getTag());
                if (position == 5) {
                    progress.show();
                    getRealTimeParkingSpaces();
                    ddbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return false;
                } else {
                    try {
                        selectedMarker = markers.getJSONObject(position);
                        String name = selectedMarker.getString("name");
                        String address = selectedMarker.getString("address");
                        String contact = selectedMarker.getString("contact");

                        slotTitle.setText(name);
                        slotAddress.setText(address);
                        slotContact.setText(contact);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return false;
                }
            }
        });

    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if(ddbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            ddbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    public JSONArray getMarkers() {
        JSONArray markers = new JSONArray();
        JSONObject marker1 = new JSONObject();
        JSONObject marker2 = new JSONObject();
        JSONObject marker3 = new JSONObject();
        JSONObject marker4 = new JSONObject();
        JSONObject marker5 = new JSONObject();
        try {
            marker1.put("id", "0");
            marker1.put("name", "Worship");
            marker1.put("address", "Gawade Mala, Bhistabag, Ahmednagar, Maharashtra 414003");
            marker1.put("contact", "+91 9556452367");
            marker1.put("lat", "19.1294128");
            marker1.put("lng", "74.7305928");
            marker1.put("parking","full");

            marker2.put("id", "1");
            marker2.put("name", "Sai Anand Lawns");
            marker2.put("address", "SH 44,State Highway 44, Ahmednagar, Maharashtra 414601");
            marker2.put("contact", "+91 7685676534");
            marker2.put("lat", "19.1586916");
            marker2.put("lng", "74.7593276");
            marker2.put("parking","full");

            marker3.put("id", "2");
            marker3.put("name", "Ahmednagar College");
            marker3.put("address", "Chandni Chowk, Station Rd, M.I.R.C Ahmednagar, Ahmednagar, Maharashtra 414001");
            marker3.put("contact", "+91 8956452367");
            marker3.put("lat", "19.087487");
            marker3.put("lng", "74.7504075");
            marker3.put("parking","full");

            marker4.put("id", "3");
            marker4.put("name", "Shilpa Garden");
            marker4.put("address", "SH 10, SH10, Maniknagar, Ahmednagar, Maharashtra 414001");
            marker4.put("contact", "+91 9755629987");
            marker4.put("lat", "19.0734921");
            marker4.put("lng", "74.7271563");
            marker4.put("parking","full");

            marker5.put("id", "4");
            marker5.put("name", "Bank of Baroda - Ahmednagar Branch");
            marker5.put("address", "P-71, MIDC, Ahmednagar, Maharashtra 414111");
            marker5.put("contact", "+91 7856432456");
            marker5.put("lat", "19.1479559");
            marker5.put("lng", "74.7011448");
            marker5.put("parking","full");

            markers.put(marker1);
            markers.put(marker2);
            markers.put(marker3);
            markers.put(marker4);
            markers.put(marker5);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return markers;
    }

    public void getRealTimeParkingSpaces() {
        updateParkingSpaceStatus(1);
        updateParkingSpaceStatus(2);
        updateParkingSpaceStatus(3);
        updateParkingSpaceStatus(4);
        updateParkingSpaceStatus(5);
    }

    public void updateParkingSpaceStatus(final int id){
        String url = "https://api.thingspeak.com/channels/692803/fields/"+id+"/last.json?api_key=31917643W0NQ2PWE";
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress.dismiss();
                // Display the first 500 characters of the response string.
                Log.d("AK",response);
                try {
                    JSONObject resp = new JSONObject(response);
                    int status = resp.getInt("field"+id);
                    if (status == 1 && id == 1) {
                        parkSpace1.setImageResource(R.drawable.available);
                    }
                    if (status == 1 && id == 2) {
                        parkSpace2.setImageResource(R.drawable.available);
                    }
                    if (status == 1 && id == 3) {
                        parkSpace3.setImageResource(R.drawable.available);
                    }
                    if (status == 1 && id == 4) {
                        parkSpace4.setImageResource(R.drawable.available);
                    }
                    if (status == 1 && id == 5) {
                        parkSpace5.setImageResource(R.drawable.available);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Log.d("AK",error.toString());
                }
                });
        queue.add(stringRequest);
    }

}
