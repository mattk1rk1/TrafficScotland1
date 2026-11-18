package com.example.trafficscotland.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class SearchFragment extends Fragment {

    private final ArrayList<Situation> allSituations =
            new ArrayList<>();

    private final ArrayList<Situation> filteredSituations =
            new ArrayList<>();

    private SearchSituationAdapter adapter;

    private TrafficViewModel viewModel;

    private EditText editSearchRoad;

    private TextView txtSearchCount;
    private TextView txtSelectedCount;

    public SearchFragment() {
        super(R.layout.fragment_search);
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
                        R.id.btnSearchBack
                );

        Button selectAllButton =
                view.findViewById(
                        R.id.btnSelectAll
                );

        Button clearButton =
                view.findViewById(
                        R.id.btnClearSelection
                );

        Button mapButton =
                view.findViewById(
                        R.id.btnSaveForMap
                );

        editSearchRoad =
                view.findViewById(
                        R.id.editSearchRoad
                );

        txtSearchCount =
                view.findViewById(
                        R.id.txtSearchCount
                );

        txtSelectedCount =
                view.findViewById(
                        R.id.txtSelectedCount
                );

        RecyclerView recyclerView =
                view.findViewById(
                        R.id.recyclerSearch
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        adapter =
                new SearchSituationAdapter(
                        filteredSituations,
                        this::openDetails,
                        this::updateSelectedCount
                );

        recyclerView.setAdapter(
                adapter
        );

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

                            filterSituations(
                                    editSearchRoad
                                            .getText()
                                            .toString()
                            );
                        }
                );

        editSearchRoad.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        filterSituations(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        selectAllButton.setOnClickListener(v -> {

            if (filteredSituations.isEmpty()) {

                Toast.makeText(
                        requireContext(),
                        "No search results to select",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            adapter.selectAllVisible();
        });

        clearButton.setOnClickListener(v -> {

            adapter.clearSelection();
        });

        mapButton.setOnClickListener(v -> {

            showSelectedOnMap();
        });

        backButton.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }

    private void filterSituations(
            String searchText) {

        filteredSituations.clear();

        String search =
                searchText
                        .trim()
                        .toLowerCase(
                                Locale.UK
                        );

        if (search.isEmpty()) {

            txtSearchCount.setText(
                    "Enter a road to search"
            );

            adapter.notifyDataSetChanged();

            return;
        }

        for (Situation situation :
                allSituations) {

            String road =
                    situation.getRoadName();

            String description =
                    situation.getDescription();

            if (road == null) {
                road = "";
            }

            if (description == null) {
                description = "";
            }

            String roadLower =
                    road.toLowerCase(
                            Locale.UK
                    );

            String descriptionLower =
                    description.toLowerCase(
                            Locale.UK
                    );

            /*
             * Search the parsed road name first.
             *
             * Also search the general description because
             * some DATEX records may contain the road name
             * in the public comment even when roadName is
             * missing or incomplete.
             */
            if (roadLower.contains(search)
                    ||
                    descriptionLower.contains(search)) {

                filteredSituations.add(
                        situation
                );
            }
        }

        txtSearchCount.setText(
                filteredSituations.size()
                        + " situations found"
        );

        adapter.notifyDataSetChanged();
    }

    private void updateSelectedCount(
            int count) {

        txtSelectedCount.setText(
                count + " selected"
        );
    }

    private void showSelectedOnMap() {

        ArrayList<Situation> selected =
                adapter.getSelectedSituations();

        if (selected.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Select at least one situation",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Store the selected situations in the shared
         * Activity ViewModel.
         *
         * TrafficMapFragment will read these and show
         * only the selected markers.
         */
        viewModel.setSelectedMapSituations(
                selected
        );

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        new TrafficMapFragment()
                )
                .addToBackStack(null)
                .commit();
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