package com.example.trafficscotland.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficscotland.R;
import com.example.trafficscotland.model.Situation;
import com.example.trafficscotland.viewmodel.TrafficViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class TrafficMapFragment
        extends Fragment
        implements OnMapReadyCallback {

    private GoogleMap googleMap;

    private TrafficViewModel viewModel;

    private TextView txtMapMode;

    private List<Situation> allSituations =
            new ArrayList<>();

    private List<Situation> selectedSituations =
            new ArrayList<>();

    private boolean mapReady = false;

    public TrafficMapFragment() {
        super(R.layout.fragment_map);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        Button backButton =
                view.findViewById(
                        R.id.btnMapBack
                );

        txtMapMode =
                view.findViewById(
                        R.id.txtMapMode
                );

        viewModel =
                new ViewModelProvider(
                        requireActivity()
                ).get(
                        TrafficViewModel.class
                );

        backButton.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );

        /*
         * Create the Google Map fragment
         * inside our existing Fragment.
         */
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getChildFragmentManager()
                                .findFragmentById(
                                        R.id.mapContainer
                                );

        if (mapFragment == null) {

            mapFragment =
                    SupportMapFragment.newInstance();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.mapContainer,
                            mapFragment
                    )
                    .commit();
        }

        mapFragment.getMapAsync(
                this
        );

        /*
         * Observe all live Traffic Scotland
         * situations.
         */
        viewModel.getSituations()
                .observe(
                        getViewLifecycleOwner(),
                        situations -> {

                            if (situations != null) {

                                allSituations =
                                        situations;

                            } else {

                                allSituations =
                                        new ArrayList<>();
                            }

                            displaySituations();
                        }
                );

        /*
         * Observe situations selected
         * from Search by Road.
         */
        viewModel.getSelectedMapSituations()
                .observe(
                        getViewLifecycleOwner(),
                        situations -> {

                            if (situations != null) {

                                selectedSituations =
                                        situations;

                            } else {

                                selectedSituations =
                                        new ArrayList<>();
                            }

                            displaySituations();
                        }
                );
    }

    @Override
    public void onMapReady(
            @NonNull GoogleMap map) {

        googleMap = map;

        mapReady = true;

        /*
         * MAP CONTROLS
         */

        // Visible + and - buttons
        googleMap
                .getUiSettings()
                .setZoomControlsEnabled(
                        true
                );

        // Pinch zoom
        googleMap
                .getUiSettings()
                .setZoomGesturesEnabled(
                        true
                );

        // Drag around the map
        googleMap
                .getUiSettings()
                .setScrollGesturesEnabled(
                        true
                );

        // Rotate map with gestures
        googleMap
                .getUiSettings()
                .setRotateGesturesEnabled(
                        true
                );

        // Tilt map
        googleMap
                .getUiSettings()
                .setTiltGesturesEnabled(
                        true
                );

        // Show compass when map is rotated
        googleMap
                .getUiSettings()
                .setCompassEnabled(
                        true
                );

        googleMap.setMapType(
                GoogleMap.MAP_TYPE_NORMAL
        );

        /*
         * Clicking a marker first shows
         * its information window.
         *
         * Clicking the information window
         * opens the full Details screen.
         */
        googleMap.setOnInfoWindowClickListener(
                marker -> {

                    Object tag =
                            marker.getTag();

                    if (tag instanceof Situation) {

                        Situation situation =
                                (Situation) tag;

                        openDetails(
                                situation
                        );
                    }
                }
        );

        displaySituations();
    }

    private void displaySituations() {

        if (!mapReady ||
                googleMap == null) {

            return;
        }

        googleMap.clear();

        List<Situation> situationsToShow;

        /*
         * If the user selected situations
         * through Search by Road, show those.
         *
         * Otherwise show everything.
         */
        if (selectedSituations != null &&
                !selectedSituations.isEmpty()) {

            situationsToShow =
                    selectedSituations;

            txtMapMode.setText(
                    "Showing "
                            + selectedSituations.size()
                            + " selected situations"
            );

        } else {

            situationsToShow =
                    allSituations;

            txtMapMode.setText(
                    "Showing all "
                            + allSituations.size()
                            + " situations"
            );
        }

        if (situationsToShow == null ||
                situationsToShow.isEmpty()) {

            moveToScotland();

            return;
        }

        LatLngBounds.Builder boundsBuilder =
                new LatLngBounds.Builder();

        int markerCount = 0;

        LatLng firstPosition = null;

        for (Situation situation :
                situationsToShow) {

            double latitude =
                    situation.getLatitude();

            double longitude =
                    situation.getLongitude();

            if (!validCoordinate(
                    latitude,
                    longitude)) {

                continue;
            }

            LatLng position =
                    new LatLng(
                            latitude,
                            longitude
                    );

            String road =
                    situation.getRoadName();

            if (road == null ||
                    road.trim().isEmpty()) {

                road = "Unknown road";
            }

            String type =
                    situation.getType();

            if (type == null ||
                    type.trim().isEmpty()) {

                type =
                        situation.getCategory();
            }

            if (type == null ||
                    type.trim().isEmpty()) {

                type = "Traffic situation";
            }

            String description =
                    situation.getDescription();

            if (description == null) {
                description = "";
            }

            float markerColour =
                    getMarkerColour(
                            situation
                    );

            Marker marker =
                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(
                                            position
                                    )
                                    .title(
                                            road
                                                    + " - "
                                                    + type
                                    )
                                    .snippet(
                                            description
                                    )
                                    .icon(
                                            BitmapDescriptorFactory
                                                    .defaultMarker(
                                                            markerColour
                                                    )
                                    )
                    );

            if (marker != null) {

                /*
                 * Store the Situation inside
                 * the marker so we know which
                 * Details page to open.
                 */
                marker.setTag(
                        situation
                );
            }

            boundsBuilder.include(
                    position
            );

            if (firstPosition == null) {

                firstPosition =
                        position;
            }

            markerCount++;

            /*
             * Some Traffic Scotland situations
             * have FROM and TO coordinates.
             *
             * Draw a line between those points.
             */
            drawSituationLine(
                    situation,
                    boundsBuilder
            );
        }

        if (markerCount == 0) {

            moveToScotland();

            return;
        }

        /*
         * If only one situation is shown,
         * zoom directly to it.
         */
        if (markerCount == 1 &&
                firstPosition != null) {

            googleMap.animateCamera(
                    CameraUpdateFactory
                            .newLatLngZoom(
                                    firstPosition,
                                    13f
                            )
            );

            return;
        }

        /*
         * Multiple situations:
         * zoom so they all fit on screen.
         */
        googleMap.setOnMapLoadedCallback(
                () -> {

                    try {

                        googleMap.animateCamera(
                                CameraUpdateFactory
                                        .newLatLngBounds(
                                                boundsBuilder.build(),
                                                100
                                        )
                        );

                    } catch (Exception e) {

                        moveToScotland();
                    }
                }
        );
    }

    private void drawSituationLine(
            Situation situation,
            LatLngBounds.Builder boundsBuilder) {

        double fromLatitude =
                situation.getFromLatitude();

        double fromLongitude =
                situation.getFromLongitude();

        double toLatitude =
                situation.getToLatitude();

        double toLongitude =
                situation.getToLongitude();

        /*
         * Both endpoints must exist.
         */
        if (!validCoordinate(
                fromLatitude,
                fromLongitude)) {

            return;
        }

        if (!validCoordinate(
                toLatitude,
                toLongitude)) {

            return;
        }

        /*
         * Don't draw a line when the two
         * coordinates are effectively
         * identical.
         */
        if (Math.abs(
                fromLatitude - toLatitude)
                < 0.000001
                &&
                Math.abs(
                        fromLongitude
                                - toLongitude)
                        < 0.000001) {

            return;
        }

        LatLng from =
                new LatLng(
                        fromLatitude,
                        fromLongitude
                );

        LatLng to =
                new LatLng(
                        toLatitude,
                        toLongitude
                );

        int lineColour =
                Color.RED;

        /*
         * Roadworks = orange.
         * Incidents = red.
         */
        if (situation.getCategory() != null &&
                situation.getCategory()
                        .equalsIgnoreCase(
                                "Roadwork"
                        )) {

            lineColour =
                    Color.rgb(
                            255,
                            152,
                            0
                    );
        }

        googleMap.addPolyline(
                new PolylineOptions()
                        .add(
                                from,
                                to
                        )
                        .width(
                                7f
                        )
                        .color(
                                lineColour
                        )
        );

        boundsBuilder.include(
                from
        );

        boundsBuilder.include(
                to
        );
    }

    private float getMarkerColour(
            Situation situation) {

        /*
         * Orange marker = roadwork
         */
        if (situation.getCategory() != null &&
                situation.getCategory()
                        .equalsIgnoreCase(
                                "Roadwork"
                        )) {

            return BitmapDescriptorFactory
                    .HUE_ORANGE;
        }

        /*
         * Red marker = incident
         */
        return BitmapDescriptorFactory
                .HUE_RED;
    }

    private boolean validCoordinate(
            double latitude,
            double longitude) {

        return latitude >= -90
                && latitude <= 90
                && longitude >= -180
                && longitude <= 180
                && !(latitude == 0
                && longitude == 0);
    }

    private void moveToScotland() {

        LatLng scotland =
                new LatLng(
                        56.4907,
                        -4.2026
                );

        googleMap.moveCamera(
                CameraUpdateFactory
                        .newLatLngZoom(
                                scotland,
                                6f
                        )
        );
    }

    private void openDetails(
            Situation situation) {

        viewModel.selectSituation(
                situation
        );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new DetailsFragment()
                )
                .addToBackStack(null)
                .commit();
    }
}