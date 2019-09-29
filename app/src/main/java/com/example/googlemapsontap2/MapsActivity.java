package com.example.googlemapsontap2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Header;
import com.example.googlemapsontap2.googleDirection.MapRoute;
import com.example.googlemapsontap2.googleDirection.SearchDirection;
import com.example.googlemapsontap2.googleDirection.SearchDirectionListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.novoda.merlin.Bindable;
import com.novoda.merlin.Connectable;
import com.novoda.merlin.Disconnectable;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.NetworkStatus;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SearchDirectionListener {

    private  Location location;
    private GoogleMap mMap;
    CardView layoutBottomSheet;
    BottomSheetBehavior sheetBehavior;

    private TextView tvDiaChi;
    private Button btnChiDuong;

    private FusedLocationProviderClient mfusedLocationProviderClient;
    private PlacesClient mplacesClient;
    private List<AutocompletePrediction> predictionList;
    private LocationCallback locationCallback;
    private MaterialSearchBar materialSearchBar;
    private View mapView;


    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private Merlin merlin;

    String sss ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        AnhXa();
        getDeviceLocation();




    }
    private void AnhXa() {
        tvDiaChi = (TextView) findViewById(R.id.tvDiaChi);
        btnChiDuong = (Button) findViewById(R.id.btnChiDuong);
        layoutBottomSheet = findViewById(R.id.bottom_sheet);
        materialSearchBar = findViewById(R.id.searchBar);
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();

        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        Places.initialize(MapsActivity.this,"AIzaSyA9rTnxqBiap6yioOqcGREcyrcZno2ShGU");
        mplacesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();


        // chuc nang trong materialSerchBar;

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(),true,null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION){
                    // mo NAVIGATION

                }else if (buttonCode == MaterialSearchBar.BUTTON_BACK){
                    materialSearchBar.disableSearch();
                    sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        // hien thi list danh sach tim kiem

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                        phạm vi hàn nội
//                        .setLocationRestriction(RectangularBounds.newInstance(
//                                new LatLng(21.027379, 105.836965),
//                                new LatLng(21.027379, 105.836965)))
                        .setCountry("vn")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(charSequence.toString())
                        .build();

                mplacesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()){
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null){
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> stringList = new ArrayList<>();
                                for (int i=0; i<predictionList.size();i++){
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    stringList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(stringList);
                                if (!materialSearchBar.isSuggestionsVisible()){
                                    // không tìm ký tự tìm kiếm
                                    materialSearchBar.clearSuggestions();
                                }
                            }

                        }else {

                        }
                    }
                });
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()){
                    return;
                }

                AutocompletePrediction selectedPrediction = predictionList.get(position);
                final String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                },1000);


                InputMethodManager inm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (inm != null){
                    inm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
                    String placeId = selectedPrediction.getPlaceId();
                    List<Place.Field> placeField = Arrays.asList(Place.Field.LAT_LNG,Place.Field.NAME);

                    FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId,placeField).build();
                    mplacesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            final Place place = fetchPlaceResponse.getPlace();
                            btnChiDuong.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (location == null){
                                        Toast.makeText(MapsActivity.this, "Chưa xác định được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                                    }else {
                                        final  String latlngdau = place.getLatLng().latitude + "," + place.getLatLng().longitude;
                                        try {
                                            new SearchDirection(MapsActivity.this, sss, latlngdau).execute();
                                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                            tvDiaChi.setText(place.getName());
                            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException){
                                ApiException apiException = (ApiException) e;
                                apiException.printStackTrace();
                                int statusCode = apiException.getStatusCode();
                                //  lỗi
                            }
                        }
                    });
                }
            }
            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);


            if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){
                View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                layoutParams.setMargins(0,0,40,180);
            }

        // check gps và cập nhật api liên tục
            LocationRequest locationRequest =  LocationRequest.create();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            SettingsClient settingsClient = LocationServices.getSettingsClient(MapsActivity.this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

            task.addOnSuccessListener(MapsActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    getDeviceLocation();
                }
            });

            // hàm tác gọi đến onActivityResult
            task.addOnFailureListener(MapsActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    if (e instanceof  ResolvableApiException){
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        try {
                            resolvable.startResolutionForResult(MapsActivity.this, 51);
                        } catch (IntentSender.SendIntentException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });

    }

    // hàm trả về kết quả
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51){
            if (requestCode == RESULT_OK){
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mfusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()){
                    location = task.getResult();
                    if (location != null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
                        sss = location.getLatitude() + "," + location.getLongitude();
                    }else {
                        LocationRequest locationRequest =  LocationRequest.create();
                        locationRequest.setInterval(1000);
                        locationRequest.setFastestInterval(1000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null){
                                    return;
                                }
                                location = locationResult.getLastLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
                                sss = location.getLatitude() + "," + location.getLongitude();
                                mfusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        mfusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);

                    }
                }else {
                    // không bật gps lên
                }
            }
        });
    }

    @Override
    public void onDirectionFinderStart() {
       progressDialog = new ProgressDialog(MapsActivity.this);
       progressDialog.setMessage("Dowloading...");
       progressDialog.cancel();
       progressDialog.show();

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }

    }


    @Override
    public void onDirectionFinderSuccess(final List<MapRoute> mapRoute) {
        PolyLine(mapRoute);

        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                PolyLine(mapRoute);
            }
        });

        }

//    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
//        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
//        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
//        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        vectorDrawable.draw(canvas);
//        return BitmapDescriptorFactory.fromBitmap(bitmap);
//    }
    private void PolyLine(List<MapRoute> mapRoute){

        progressDialog.dismiss();
        for (MapRoute mapRoute1 : mapRoute) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapRoute1.startLocation, 12));
            tvDiaChi.setText(mapRoute1.endAddress
                    + "\n" + "Thời gian : " + mapRoute1.mapDuration.txtDuration
                    + "\n" + "Quãng đường :" + mapRoute1.mapDistance.txtDistance);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title(mapRoute1.startAddress)
                    .position(mapRoute1.startLocation)));

            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title(mapRoute1.endAddress).rotation(0)
                    .position(mapRoute1.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.rgb(50, 50, 255)).
                    width(10);

            for (int i = 0; i < mapRoute1.points.size(); i++)
                polylineOptions.add(mapRoute1.points.get(i));
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    protected void onResume() {
    super.onResume();
    merlin.bind(); }

    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
    }

}
