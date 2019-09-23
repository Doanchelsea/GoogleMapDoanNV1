package com.example.googlemapsontap2.googleDirection;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class MapRoute {
    public MapDistance mapDistance;
    public MapDuration mapDuration;

    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
}
