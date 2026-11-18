package com.example.trafficscotland.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficscotland.R;
import com.example.trafficscotland.model.Situation;

import java.util.ArrayList;
import java.util.List;

public class SearchSituationAdapter
        extends RecyclerView.Adapter<SearchSituationAdapter.ViewHolder> {

    public interface OnSituationClickListener {

        void onSituationClick(
                Situation situation
        );
    }

    public interface OnSelectionChangedListener {

        void onSelectionChanged(
                int selectedCount
        );
    }

    private final ArrayList<Situation> situations;

    private final ArrayList<Situation>
            selectedSituations;

    private final OnSituationClickListener
            clickListener;

    private final OnSelectionChangedListener
            selectionListener;

    public SearchSituationAdapter(
            ArrayList<Situation> situations,
            OnSituationClickListener clickListener,
            OnSelectionChangedListener selectionListener) {

        this.situations =
                situations;

        this.clickListener =
                clickListener;

        this.selectionListener =
                selectionListener;

        selectedSituations =
                new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_search_situation,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Situation situation =
                situations.get(position);

        String category =
                situation.getCategory();

        String type =
                situation.getType();

        String road =
                situation.getRoadName();

        String description =
                situation.getDescription();

        if (category == null) {
            category = "";
        }

        if (type == null) {
            type = "";
        }

        if (road == null ||
                road.isEmpty()) {

            road =
                    "Unknown road";
        }

        if (description == null) {
            description = "";
        }

        holder.txtCategory.setText(
                category + " - " + type
        );

        holder.txtRoad.setText(
                road
        );

        holder.txtDescription.setText(
                description
        );

        holder.checkSelected
                .setOnCheckedChangeListener(
                        null
                );

        holder.checkSelected.setChecked(
                selectedSituations.contains(
                        situation
                )
        );

        holder.checkSelected
                .setOnCheckedChangeListener(
                        (buttonView, isChecked) -> {

                            if (isChecked) {

                                if (!selectedSituations
                                        .contains(
                                                situation
                                        )) {

                                    selectedSituations
                                            .add(
                                                    situation
                                            );
                                }

                            } else {

                                selectedSituations
                                        .remove(
                                                situation
                                        );
                            }

                            notifySelectionChanged();
                        }
                );

        // Clicking the text/card opens Details.
        holder.itemView.setOnClickListener(v -> {

            if (clickListener != null) {

                clickListener
                        .onSituationClick(
                                situation
                        );
            }
        });

        // Clicking directly on checkbox
        // only selects/deselects it.
        holder.checkSelected
                .setOnClickListener(v -> {
                    // handled by checked listener
                });
    }

    @Override
    public int getItemCount() {

        return situations.size();
    }

    public ArrayList<Situation>
    getSelectedSituations() {

        return new ArrayList<>(
                selectedSituations
        );
    }

    public void selectAllVisible() {

        for (Situation situation :
                situations) {

            if (!selectedSituations
                    .contains(
                            situation
                    )) {

                selectedSituations.add(
                        situation
                );
            }
        }

        notifyDataSetChanged();

        notifySelectionChanged();
    }

    public void clearSelection() {

        selectedSituations.clear();

        notifyDataSetChanged();

        notifySelectionChanged();
    }

    private void notifySelectionChanged() {

        if (selectionListener != null) {

            selectionListener
                    .onSelectionChanged(
                            selectedSituations.size()
                    );
        }
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtCategory;
        TextView txtRoad;
        TextView txtDescription;

        CheckBox checkSelected;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtCategory =
                    itemView.findViewById(
                            R.id.txtSearchCategory
                    );

            txtRoad =
                    itemView.findViewById(
                            R.id.txtSearchRoad
                    );

            txtDescription =
                    itemView.findViewById(
                            R.id.txtSearchDescription
                    );

            checkSelected =
                    itemView.findViewById(
                            R.id.checkSearchSelected
                    );
        }
    }
}