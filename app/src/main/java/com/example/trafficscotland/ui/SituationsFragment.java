package com.example.trafficscotland.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class SituationsFragment extends Fragment {

    private ArrayList<Situation> incidents;
    private SituationAdapter adapter;
    private TrafficViewModel viewModel;

    public SituationsFragment() {
        super(R.layout.fragment_situations);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        Log.d(
                "TrafficAPI",
                "SituationsFragment opened"
        );

        TextView title =
                view.findViewById(R.id.txtTitle);

        title.setText("Current Incidents");

        Button backButton =
                view.findViewById(R.id.btnBack);

        RecyclerView recyclerView =
                view.findViewById(
                        R.id.recyclerSituations
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        incidents = new ArrayList<>();

        Log.d(
                "TrafficAPI",
                "Creating TrafficViewModel"
        );

        viewModel =
                new ViewModelProvider(
                        requireActivity()
                ).get(TrafficViewModel.class);

        adapter =
                new SituationAdapter(
                        incidents,
                        this::openDetails
                );

        recyclerView.setAdapter(adapter);

        viewModel.getSituations().observe(
                getViewLifecycleOwner(),
                situations -> {

                    Log.d(
                            "TrafficAPI",
                            "ViewModel returned "
                                    + situations.size()
                                    + " total situations"
                    );

                    incidents.clear();

                    for (Situation situation :
                            situations) {

                        if (situation.getCategory() != null
                                && situation
                                .getCategory()
                                .equalsIgnoreCase(
                                        "Incident"
                                )) {

                            incidents.add(
                                    situation
                            );
                        }
                    }

                    Log.d(
                            "TrafficAPI",
                            "Incidents displayed: "
                                    + incidents.size()
                    );

                    adapter.notifyDataSetChanged();
                }
        );

        viewModel.getErrorMessage().observe(
                getViewLifecycleOwner(),
                error -> {

                    if (error != null &&
                            !error.isEmpty()) {

                        Log.e(
                                "TrafficAPI",
                                "ViewModel error: "
                                        + error
                        );
                    }
                }
        );

        backButton.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack()
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