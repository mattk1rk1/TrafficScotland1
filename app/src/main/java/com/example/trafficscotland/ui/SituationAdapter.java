package com.example.trafficscotland.ui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trafficscotland.R;
import com.example.trafficscotland.model.Situation;

import java.util.List;
import java.util.Locale;

public class SituationAdapter
        extends RecyclerView.Adapter<SituationAdapter.SituationViewHolder> {

    public interface OnSituationClickListener {
        void onSituationClick(Situation situation);
    }

    private final List<Situation> situations;
    private final OnSituationClickListener listener;

    public SituationAdapter(
            List<Situation> situations,
            OnSituationClickListener listener) {

        this.situations = situations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SituationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_situation,
                                parent,
                                false
                        );

        return new SituationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SituationViewHolder holder,
            int position) {

        Situation situation =
                situations.get(position);

        String type =
                safe(
                        situation.getType()
                );

        String road =
                safe(
                        situation.getRoadName()
                );

        String description =
                safe(
                        situation.getDescription()
                );

        if (type.isEmpty()) {
            type = "Traffic Situation";
        }

        if (road.isEmpty()) {
            road = "Unknown road";
        }

        holder.txtType.setText(
                type
        );

        holder.txtRoad.setText(
                road
        );

        holder.txtDescription.setText(
                description
        );

        setSituationIcon(
                holder.imgSituationIcon,
                situation
        );

        setRoadworkDelayBadge(
                holder.txtDelayDuration,
                situation
        );

        holder.itemView.setOnClickListener(v -> {

            if (listener != null) {

                listener.onSituationClick(
                        situation
                );
            }
        });
    }

    private void setRoadworkDelayBadge(
            TextView badge,
            Situation situation) {

        String category =
                safe(
                        situation.getCategory()
                ).toLowerCase(
                        Locale.UK
                );

        String type =
                safe(
                        situation.getType()
                ).toLowerCase(
                        Locale.UK
                );

        String delay =
                safe(
                        situation.getDelayType()
                );

        String delayLower =
                delay.toLowerCase(
                        Locale.UK
                );

        boolean isRoadwork =
                category.contains("roadwork")
                        ||
                        type.contains("roadwork");

        if (!isRoadwork) {

            badge.setVisibility(
                    View.GONE
            );

            return;
        }

        badge.setVisibility(
                View.VISIBLE
        );

        /*
         * Traffic Scotland supplies values
         * such as "Long Delays".
         */
        if (delayLower.contains("long")) {

            setBadgeStyle(
                    badge,
                    "LONG DELAY",
                    Color.rgb(
                            198,
                            40,
                            40
                    )
            );

        } else if (delayLower.contains("delay")) {

            setBadgeStyle(
                    badge,
                    "SHORT DELAY",
                    Color.rgb(
                            46,
                            125,
                            50
                    )
            );

        } else {

            setBadgeStyle(
                    badge,
                    "NO DELAY INFO",
                    Color.rgb(
                            117,
                            117,
                            117
                    )
            );
        }
    }

    private void setBadgeStyle(
            TextView badge,
            String text,
            int colour) {

        badge.setText(
                text
        );

        badge.setTextColor(
                Color.WHITE
        );

        /*
         * Create an actual drawable background.
         * This fixes the invisible white-text issue.
         */
        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                colour
        );

        float radius =
                8 * badge
                        .getResources()
                        .getDisplayMetrics()
                        .density;

        background.setCornerRadius(
                radius
        );

        badge.setBackground(
                background
        );

        int horizontalPadding =
                (int) (
                        10 * badge
                                .getResources()
                                .getDisplayMetrics()
                                .density
                );

        int verticalPadding =
                (int) (
                        4 * badge
                                .getResources()
                                .getDisplayMetrics()
                                .density
                );

        badge.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding
        );
    }

    private void setSituationIcon(
            ImageView imageView,
            Situation situation) {

        String text =
                (
                        safe(
                                situation.getType()
                        )
                                + " "
                                + safe(
                                situation.getDescription()
                        )
                ).toLowerCase(
                        Locale.UK
                );

        String category =
                safe(
                        situation.getCategory()
                ).toLowerCase(
                        Locale.UK
                );

        int icon;

        if (text.contains("collision")
                ||
                text.contains("accident")) {

            icon =
                    R.drawable.ic_collision;

        } else if (
                text.contains("breakdown")
                        ||
                        text.contains(
                                "broken down"
                        )) {

            icon =
                    R.drawable.ic_breakdown;

        } else if (
                text.contains("closure")
                        ||
                        text.contains("closed")) {

            icon =
                    R.drawable.ic_closure;

        } else if (
                text.contains("weather")
                        ||
                        text.contains("snow")
                        ||
                        text.contains("ice")
                        ||
                        text.contains("flood")
                        ||
                        text.contains("wind")) {

            icon =
                    R.drawable.ic_weather;

        } else if (
                text.contains("roadwork")
                        ||
                        category.contains("roadwork")) {

            icon =
                    R.drawable.ic_roadworks;

        } else {

            icon =
                    R.drawable.ic_obstruction;
        }

        imageView.setImageResource(
                icon
        );
    }

    private String safe(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }

    @Override
    public int getItemCount() {

        return situations.size();
    }

    static class SituationViewHolder
            extends RecyclerView.ViewHolder {

        ImageView imgSituationIcon;

        TextView txtType;
        TextView txtRoad;
        TextView txtDescription;
        TextView txtDelayDuration;

        public SituationViewHolder(
                @NonNull View itemView) {

            super(itemView);

            imgSituationIcon =
                    itemView.findViewById(
                            R.id.imgSituationIcon
                    );

            txtType =
                    itemView.findViewById(
                            R.id.txtType
                    );

            txtRoad =
                    itemView.findViewById(
                            R.id.txtRoad
                    );

            txtDescription =
                    itemView.findViewById(
                            R.id.txtDescription
                    );

            txtDelayDuration =
                    itemView.findViewById(
                            R.id.txtDelayDuration
                    );
        }
    }
}