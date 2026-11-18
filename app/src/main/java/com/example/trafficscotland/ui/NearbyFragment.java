package com.example.trafficscotland.ui;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficscotland.R;
import com.example.trafficscotland.model.Situation;
import com.example.trafficscotland.viewmodel.TrafficViewModel;

import java.util.ArrayList;
import java.util.Comparator;

public class NearbyFragment extends Fragment {

    // Fixed Glasgow city centre location
    private static final double GLASGOW_LATITUDE =
            55.8642;

    private static final double GLASGOW_LONGITUDE =
            -4.2518;

    private final ArrayList<Situation> allSituations =
            new ArrayList<>();

    private final ArrayList<Situation> nearbySituations =
            new ArrayList<>();

    private SituationAdapter adapter;

    private TrafficViewModel viewModel;

    private Spinner spinnerDistance;

    private TextView txtNearbyCount;

    public NearbyFragment() {

        super(R.layout.fragment_nearby);
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
                        R.id.btnNearbyBack
                );

        TextView txtLocation =
                view.findViewById(
                        R.id.txtLocation
                );

        txtNearbyCount =
                view.findViewById(
                        R.id.txtNearbyCount
                );

        spinnerDistance =
                view.findViewById(
                        R.id.spinnerDistance
                );

        RecyclerView recyclerView =
                view.findViewById(
                        R.id.recyclerNearby
                );

        // Show the fixed location being used
        txtLocation.setText(
                "Search location: Glasgow city centre\n"
                        + "55.8642, -4.2518"
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        adapter =
                new SituationAdapter(
                        nearbySituations,
                        this::openDetails
                );

        recyclerView.setAdapter(
                adapter
        );

        setupDistanceSpinner();

        viewModel =
                new ViewModelProvider(
                        requireActivity()
                ).get(
                        TrafficViewModel.class
                );

        viewModel.getSituations()
                .observe(
                        getViewLifecycleOwner(),
                        situations -> {

                            allSituations.clear();

                            if (situations != null) {

                                allSituations.addAll(
                                        situations
                                );
                            }

                            filterNearbySituations();
                        }
                );

        backButton.setOnClickListener(v ->

                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
        );
    }

    private void setupDistanceSpinner() {

        String[] distances = {
                "5 km",
                "10 km",
                "25 km",
                "50 km"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        distances
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerDistance.setAdapter(
                spinnerAdapter
        );

        // Default to 25 km
        spinnerDistance.setSelection(
                2
        );

        spinnerDistance.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        filterNearbySituations();
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    private void filterNearbySituations() {

        if (adapter == null ||
                spinnerDistance == null) {

            return;
        }

        nearbySituations.clear();

        double maxDistance =
                getSelectedDistance();

        for (Situation situation :
                allSituations) {

            double latitude =
                    situation.getLatitude();

            double longitude =
                    situation.getLongitude();

            if (latitude == 0 ||
                    longitude == 0) {

                continue;
            }

            double distance =
                    calculateDistance(
                            GLASGOW_LATITUDE,
                            GLASGOW_LONGITUDE,
                            latitude,
                            longitude
                    );

            if (distance <= maxDistance) {

                nearbySituations.add(
                        situation
                );
            }
        }

        // Sort nearest first
        nearbySituations.sort(
                Comparator.comparingDouble(
                        situation ->
                                calculateDistance(
                                        GLASGOW_LATITUDE,
                                        GLASGOW_LONGITUDE,
                                        situation.getLatitude(),
                                        situation.getLongitude()
                                )
                )
        );

        adapter.notifyDataSetChanged();

        txtNearbyCount.setText(
                nearbySituations.size()
                        + " situations within "
                        + (int) maxDistance
                        + " km of Glasgow"
        );
    }

    private double calculateDistance(
            double startLatitude,
            double startLongitude,
            double endLatitude,
            double endLongitude) {

        float[] results =
                new float[1];

        Location.distanceBetween(
                startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                results
        );

        return results[0] / 1000.0;
    }

    private double getSelectedDistance() {

        switch (
                spinnerDistance
                        .getSelectedItemPosition()) {

            case 0:
                return 5;

            case 1:
                return 10;

            case 2:
                return 25;

            case 3:
                return 50;

            default:
                return 25;
        }
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