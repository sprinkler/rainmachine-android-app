package com.rainmachine.presentation.screens.location;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.rainmachine.R;
import com.rainmachine.domain.model.LocationInfo;
import com.rainmachine.presentation.activities.SprinklerActivity;

import javax.inject.Inject;

public class LocationFragment extends SupportMapFragment implements LocationContract.View {

    private static final int CIRCLE_RADIUS_METERS = 50;
    private static final float ZOOM = 17.0f;
    private static final int STROKE_WIDTH_PIXELS = 3;

    @Inject
    LocationContract.Presenter presenter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
        getMapAsync(googleMap -> googleMap.getUiSettings().setScrollGesturesEnabled(false));
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stop();
    }

    @Override
    public void render(final LocationInfo location) {
        getMapAsync(googleMap -> {
            googleMap.clear();
            LatLng latLng = new LatLng(location.latitude, location.longitude);
            googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .strokeColor(ContextCompat.getColor(getActivity(), R.color.map_circle_stroke))
                    .strokeWidth(STROKE_WIDTH_PIXELS)
                    .fillColor(ContextCompat.getColor(getActivity(), R.color.map_circle_fill))
                    .radius(CIRCLE_RADIUS_METERS));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM));
        });
    }
}
