package com.example.googlemapsontap2.googleDirection;

import java.util.List;

public interface SearchDirectionListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<MapRoute> mapRoute);
}
