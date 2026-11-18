package com.example.trafficscotland.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficscotland.R;
import com.example.trafficscotland.viewmodel.TrafficViewModel;

public class HomeFragment extends Fragment {

    private TrafficViewModel viewModel;

    public HomeFragment() {
        super(R.layout.layoutland);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );

        viewModel =
                new ViewModelProvider(
                        requireActivity()
                ).get(
                        TrafficViewModel.class
                );

        Button incidentsButton =
                view.findViewById(
                        R.id.btnIncidents
                );

        Button roadworksButton =
                view.findViewById(
                        R.id.btnRoadworks
                );

        Button searchButton =
                view.findViewById(
                        R.id.btnSearch
                );

        Button nearbyButton =
                view.findViewById(
                        R.id.btnNearby
                );

        Button mapButton =
                view.findViewById(
                        R.id.btnMap
                );

        incidentsButton.setOnClickListener(v ->
                openFragment(
                        new SituationsFragment()
                )
        );

        roadworksButton.setOnClickListener(v ->
                openFragment(
                        new RoadworksFragment()
                )
        );

        searchButton.setOnClickListener(v ->
                openFragment(
                        new SearchFragment()
                )
        );

        nearbyButton.setOnClickListener(v ->
                openFragment(
                        new NearbyFragment()
                )
        );

        mapButton.setOnClickListener(v -> {

            /*
             * Opening Map from Home means
             * display ALL situations.
             */
            viewModel.clearSelectedMapSituations();

            openFragment(
                    new TrafficMapFragment()
            );
        });
    }

    private void openFragment(
            Fragment fragment) {

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        fragment
                )
                .addToBackStack(null)
                .commit();
    }
}