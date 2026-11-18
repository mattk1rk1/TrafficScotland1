package com.example.trafficscotland.ui;

import android.os.Bundle;
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

public class RoadworksFragment extends Fragment {

    private ArrayList<Situation> roadworks;
    private SituationAdapter adapter;
    private TrafficViewModel viewModel;

    public RoadworksFragment() {
        super(R.layout.fragment_situations);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView title =
                view.findViewById(R.id.txtTitle);

        title.setText("Roadworks");

        Button backButton =
                view.findViewById(R.id.btnBack);

        RecyclerView recyclerView =
                view.findViewById(R.id.recyclerSituations);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        roadworks = new ArrayList<>();

        viewModel =
                new ViewModelProvider(requireActivity())
                        .get(TrafficViewModel.class);

        adapter =
                new SituationAdapter(
                        roadworks,
                        this::openDetails
                );

        recyclerView.setAdapter(adapter);

        viewModel.getSituations()
                .observe(
                        getViewLifecycleOwner(),
                        situations -> {

                            roadworks.clear();

                            for (Situation situation : situations) {

                                if (situation
                                        .getCategory()
                                        .equalsIgnoreCase("Roadwork")) {

                                    roadworks.add(situation);
                                }
                            }

                            adapter.notifyDataSetChanged();
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

        viewModel.selectSituation(situation);

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