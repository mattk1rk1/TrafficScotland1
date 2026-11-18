package com.example.trafficscotland.model;

public class Situation {

    private String id;

    // Incident or Roadwork
    private String category;

    // e.g. roadClosed, queuingTraffic, MaintenanceWorks
    private String type;

    private String description;
    private String roadName;
    private String impact;

    private String delayType;
    private String direction;

    private String startDate;
    private String endDate;

    private double fromLatitude;
    private double fromLongitude;

    private double toLatitude;
    private double toLongitude;

    // Keeps our existing sample data working
    public Situation(
            String id,
            String type,
            String description,
            String roadName,
            String impact,
            String startDate,
            String endDate,
            double latitude,
            double longitude) {

        this.id = id;
        this.category = type;
        this.type = type;
        this.description = description;
        this.roadName = roadName;
        this.impact = impact;
        this.delayType = "";
        this.direction = "";
        this.startDate = startDate;
        this.endDate = endDate;

        this.fromLatitude = latitude;
        this.fromLongitude = longitude;

        this.toLatitude = latitude;
        this.toLongitude = longitude;
    }

    // Constructor for real Traffic Scotland XML
    public Situation(
            String id,
            String category,
            String type,
            String description,
            String roadName,
            String impact,
            String delayType,
            String direction,
            String startDate,
            String endDate,
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude) {

        this.id = id;
        this.category = category;
        this.type = type;
        this.description = description;
        this.roadName = roadName;
        this.impact = impact;
        this.delayType = delayType;
        this.direction = direction;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fromLatitude = fromLatitude;
        this.fromLongitude = fromLongitude;
        this.toLatitude = toLatitude;
        this.toLongitude = toLongitude;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getImpact() {
        return impact;
    }

    public String getDelayType() {
        return delayType;
    }

    public String getDirection() {
        return direction;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public double getFromLatitude() {
        return fromLatitude;
    }

    public double getFromLongitude() {
        return fromLongitude;
    }

    public double getToLatitude() {
        return toLatitude;
    }

    public double getToLongitude() {
        return toLongitude;
    }

    // Existing Nearby code can continue using these.
    // If there are two points, return the midpoint.

    public double getLatitude() {

        if (fromLatitude != 0 && toLatitude != 0) {
            return (fromLatitude + toLatitude) / 2.0;
        }

        if (fromLatitude != 0) {
            return fromLatitude;
        }

        return toLatitude;
    }

    public double getLongitude() {

        if (fromLongitude != 0 && toLongitude != 0) {
            return (fromLongitude + toLongitude) / 2.0;
        }

        if (fromLongitude != 0) {
            return fromLongitude;
        }

        return toLongitude;
    }
}