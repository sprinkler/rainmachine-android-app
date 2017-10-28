package com.rainmachine.presentation.screens.nearbystations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rainmachine.R;
import com.rainmachine.domain.model.Parser;
import com.rainmachine.presentation.activities.SprinklerActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class NearbyStationsLocationFragment extends SupportMapFragment {

    @Inject
    NearbyStationsPresenter presenter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((SprinklerActivity) getActivity()).inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapAsync(googleMap -> googleMap.getUiSettings().setScrollGesturesEnabled(false));
    }

    public void updateContent(final NearbyStationsViewModel viewModel, final boolean showPersonal) {
        getMapAsync(googleMap -> {
            googleMap.clear();
            List<Marker> markers = new ArrayList<>();
            Marker marker;
            LatLng latLng = new LatLng(viewModel.currentLocationLatitude, viewModel
                    .currentLocationLongitude);
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(getBitmapDescriptor(R.drawable.ic_place_24dp_current)));
            markers.add(marker);

            for (Parser.WeatherStation station : viewModel.parser.wUndergroundParams
                    .airportStations) {
                if (!station.hasIncompleteInfo) {
                    marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(station.latitude, station.longitude))
                            .icon(getBitmapDescriptor(R.drawable.ic_place_24dp_airport)));
                    markers.add(marker);
                }
            }

            if (showPersonal) {
                for (Parser.WeatherStation station : viewModel.parser.wUndergroundParams
                        .nearbyStations) {
                    if (!station.hasIncompleteInfo) {
                        marker = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(station.latitude, station.longitude))
                                .icon(getBitmapDescriptor(R.drawable.ic_place_24dp_personal)));
                        markers.add(marker);
                    }
                }
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker1 : markers) {
                builder.include(marker1.getPosition());
            }

            final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(),
                    getResources().getDimensionPixelSize(R.dimen.padding_map_stations));
            googleMap.setOnMapLoadedCallback(() -> googleMap.animateCamera(cu));
        });
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), id);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable
                .getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable
                .getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
