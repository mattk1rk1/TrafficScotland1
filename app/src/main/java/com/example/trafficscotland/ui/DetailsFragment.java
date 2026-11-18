package com.example.trafficscotland.ui;

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

public class DetailsFragment extends Fragment {

    public DetailsFragment() {
        super(R.layout.fragment_details);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        TextView txtType =
                view.findViewById(R.id.txtDetailsType);

        TextView txtRoad =
                view.findViewById(R.id.txtDetailsRoad);

        TextView txtDescription =
                view.findViewById(R.id.txtDetailsDescription);

        TextView txtImpact =
                view.findViewById(R.id.txtDetailsImpact);

        TextView txtStart =
                view.findViewById(R.id.txtDetailsStart);

        TextView txtEnd =
                view.findViewById(R.id.txtDetailsEnd);

        TextView txtLocation =
                view.findViewById(R.id.txtDetailsLocation);

        Button backButton =
                view.findViewById(R.id.btnDetailsBack);

        TrafficViewModel viewModel =
                new ViewModelProvider(requireActivity())
                        .get(TrafficViewModel.class);

        viewModel.getSelectedSituation()
                .observe(
                        getViewLifecycleOwner(),
                        situation -> {

                            if (situation != null) {

                                displaySituation(
                                        situation,
                                        txtType,
                                        txtRoad,
                                        txtDescription,
                                        txtImpact,
                                        txtStart,
                                        txtEnd,
                                        txtLocation
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

    private void displaySituation(
            Situation situation,
            TextView txtType,
            TextView txtRoad,
            TextView txtDescription,
            TextView txtImpact,
            TextView txtStart,
            TextView txtEnd,
            TextView txtLocation) {

        txtType.setText(
                situation.getType()
        );

        txtRoad.setText(
                "Road: " +
                        situation.getRoadName()
        );

        txtDescription.setText(
                "Description:\n" +
                        situation.getDescription()
        );

        txtImpact.setText(
                "Impact:\n" +
                        situation.getImpact()
        );

        txtStart.setText(
                "Start: " +
                        situation.getStartDate()
        );

        txtEnd.setText(
                "End: " +
                        situation.getEndDate()
        );

        txtLocation.setText(
                "Location:\n" +
                        "From: " +
                        situation.getFromLatitude() +
                        ", " +
                        situation.getFromLongitude() +
                        "\nTo: " +
                        situation.getToLatitude() +
                        ", " +
                        situation.getToLongitude()
        );
    }
}