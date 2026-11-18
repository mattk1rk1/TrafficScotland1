package com.example.trafficscotland.parser;

import android.util.Log;
import android.util.Xml;

import com.example.trafficscotland.model.Situation;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrafficXmlParser {

    public ArrayList<Situation> parse(
            String xmlData,
            String category) {

        ArrayList<Situation> situations =
                new ArrayList<>();

        if (xmlData == null ||
                xmlData.trim().isEmpty()) {

            Log.e(
                    "TrafficAPI",
                    "Empty XML received for " + category
            );

            return situations;
        }

        try {

            XmlPullParser parser =
                    Xml.newPullParser();

            // Important for DATEX / SOAP XML.
            parser.setFeature(
                    XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    true
            );

            // Coursework requirement:
            // parse the downloaded String.
            parser.setInput(
                    new StringReader(xmlData)
            );

            Deque<String> tagStack =
                    new ArrayDeque<>();

            int eventType =
                    parser.getEventType();

            boolean insideRecord = false;
            boolean insideGeneralPublicComment = false;

            String situationId = "";

            String id = "";
            String type = "";
            String description = "";
            String roadName = "";
            String impact = "";
            String delayType = "";
            String direction = "";
            String startDate = "";
            String endDate = "";

            double fromLatitude = 0;
            double fromLongitude = 0;
            double toLatitude = 0;
            double toLongitude = 0;

            Double pendingLatitude = null;
            Double pendingLongitude = null;

            int coordinatePairCount = 0;
            int recordsFound = 0;

            StringBuilder commentBuilder =
                    new StringBuilder();

            StringBuilder impactBuilder =
                    new StringBuilder();

            while (eventType !=
                    XmlPullParser.END_DOCUMENT) {

                // =====================================
                // START TAG
                // =====================================

                if (eventType ==
                        XmlPullParser.START_TAG) {

                    String tag =
                            normaliseTag(
                                    parser.getName()
                            );

                    tagStack.push(tag);

                    // -----------------------------
                    // Situation
                    // -----------------------------

                    if (tag.equalsIgnoreCase(
                            "situation")) {

                        situationId =
                                findAttribute(
                                        parser,
                                        "id"
                                );
                    }

                    // -----------------------------
                    // Situation record
                    // -----------------------------

                    if (tag.equalsIgnoreCase(
                            "situationRecord")) {

                        recordsFound++;

                        insideRecord = true;

                        id =
                                findAttribute(
                                        parser,
                                        "id"
                                );

                        if (id.isEmpty()) {

                            id =
                                    situationId;
                        }

                        type =
                                cleanType(
                                        findAttribute(
                                                parser,
                                                "type"
                                        )
                                );

                        description = "";
                        roadName = "";
                        impact = "";
                        delayType = "";
                        direction = "";
                        startDate = "";
                        endDate = "";

                        fromLatitude = 0;
                        fromLongitude = 0;
                        toLatitude = 0;
                        toLongitude = 0;

                        pendingLatitude = null;
                        pendingLongitude = null;

                        coordinatePairCount = 0;

                        commentBuilder =
                                new StringBuilder();

                        impactBuilder =
                                new StringBuilder();
                    }

                    if (insideRecord &&
                            tag.equalsIgnoreCase(
                                    "generalPublicComment")) {

                        insideGeneralPublicComment =
                                true;
                    }
                }

                // =====================================
                // TEXT
                // =====================================

                else if (eventType ==
                        XmlPullParser.TEXT
                        && insideRecord
                        && !tagStack.isEmpty()) {

                    String currentTag =
                            tagStack.peek();

                    String text =
                            parser.getText();

                    if (text != null) {

                        text =
                                text.trim();
                    }

                    if (text != null &&
                            !text.isEmpty()) {

                        // -------------------------
                        // Public comment /
                        // description
                        // -------------------------

                        if (insideGeneralPublicComment &&
                                (currentTag.equalsIgnoreCase(
                                        "value")
                                        ||
                                        currentTag.equalsIgnoreCase(
                                                "generalPublicComment"))) {

                            appendText(
                                    commentBuilder,
                                    text
                            );
                        }

                        // -------------------------
                        // Validity dates
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "overallStartTime")) {

                            startDate =
                                    text;
                        }

                        else if (currentTag.equalsIgnoreCase(
                                "overallEndTime")) {

                            endDate =
                                    text;
                        }

                        // -------------------------
                        // Direction
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "tpegDirection")) {

                            direction =
                                    makeReadable(
                                            text
                                    );

                            appendImpact(
                                    impactBuilder,
                                    "Direction",
                                    direction
                            );
                        }

                        // -------------------------
                        // Delay
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "delaysType")) {

                            delayType =
                                    text;

                            appendImpact(
                                    impactBuilder,
                                    "Delay",
                                    makeReadable(
                                            text
                                    )
                            );
                        }

                        // -------------------------
                        // Incident type
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "abnormalTrafficType")) {

                            type =
                                    makeReadable(
                                            text
                                    );

                            appendImpact(
                                    impactBuilder,
                                    "Traffic",
                                    type
                            );
                        }

                        // -------------------------
                        // Roadworks type
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "roadMaintenanceType")) {

                            String readable =
                                    makeReadable(
                                            text
                                    );

                            if (!readable.isEmpty()) {

                                type =
                                        readable;
                            }

                            appendImpact(
                                    impactBuilder,
                                    "Work",
                                    readable
                            );
                        }

                        // -------------------------
                        // Closure / restrictions
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "roadOrCarriagewayOrLaneManagementType")) {

                            String readable =
                                    makeReadable(
                                            text
                                    );

                            type =
                                    readable;

                            appendImpact(
                                    impactBuilder,
                                    "Restriction",
                                    readable
                            );
                        }

                        else if (currentTag.equalsIgnoreCase(
                                "trafficConstrictionType")) {

                            appendImpact(
                                    impactBuilder,
                                    "Traffic",
                                    makeReadable(
                                            text
                                    )
                            );
                        }

                        // -------------------------
                        // Lanes
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "numberOfLanesRestricted")) {

                            appendImpact(
                                    impactBuilder,
                                    "Lanes restricted",
                                    text
                            );
                        }

                        else if (currentTag.equalsIgnoreCase(
                                "numberOfOperationalLanes")) {

                            appendImpact(
                                    impactBuilder,
                                    "Operational lanes",
                                    text
                            );
                        }

                        // -------------------------
                        // Impact
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "impactOnTraffic")) {

                            appendImpact(
                                    impactBuilder,
                                    "Impact",
                                    makeReadable(
                                            text
                                    )
                            );
                        }

                        // -------------------------
                        // Road name / number
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "roadNumber")) {

                            if (!text.isEmpty()) {

                                roadName =
                                        text;
                            }
                        }

                        else if (currentTag.equalsIgnoreCase(
                                "roadName")) {

                            if (!text.isEmpty()) {

                                roadName =
                                        text;
                            }
                        }

                        // -------------------------
                        // Coordinates
                        // -------------------------

                        else if (currentTag.equalsIgnoreCase(
                                "latitude")) {

                            pendingLatitude =
                                    parseDouble(
                                            text
                                    );

                            if (pendingLatitude != null &&
                                    pendingLongitude != null) {

                                if (coordinatePairCount == 0) {

                                    fromLatitude =
                                            pendingLatitude;

                                    fromLongitude =
                                            pendingLongitude;

                                } else if (coordinatePairCount == 1) {

                                    toLatitude =
                                            pendingLatitude;

                                    toLongitude =
                                            pendingLongitude;
                                }

                                coordinatePairCount++;

                                pendingLatitude =
                                        null;

                                pendingLongitude =
                                        null;
                            }
                        }

                        else if (currentTag.equalsIgnoreCase(
                                "longitude")) {

                            pendingLongitude =
                                    parseDouble(
                                            text
                                    );

                            if (pendingLatitude != null &&
                                    pendingLongitude != null) {

                                if (coordinatePairCount == 0) {

                                    fromLatitude =
                                            pendingLatitude;

                                    fromLongitude =
                                            pendingLongitude;

                                } else if (coordinatePairCount == 1) {

                                    toLatitude =
                                            pendingLatitude;

                                    toLongitude =
                                            pendingLongitude;
                                }

                                coordinatePairCount++;

                                pendingLatitude =
                                        null;

                                pendingLongitude =
                                        null;
                            }
                        }
                    }
                }

                // =====================================
                // END TAG
                // =====================================

                else if (eventType ==
                        XmlPullParser.END_TAG) {

                    String endTag =
                            normaliseTag(
                                    parser.getName()
                            );

                    if (endTag.equalsIgnoreCase(
                            "generalPublicComment")) {

                        insideGeneralPublicComment =
                                false;
                    }

                    if (endTag.equalsIgnoreCase(
                            "situationRecord")
                            && insideRecord) {

                        description =
                                commentBuilder
                                        .toString()
                                        .trim();

                        impact =
                                impactBuilder
                                        .toString()
                                        .trim();

                        // -------------------------
                        // Road fallback
                        // -------------------------

                        if (roadName.isEmpty()) {

                            roadName =
                                    findRoadName(
                                            description
                                    );
                        }

                        if (roadName.isEmpty()) {

                            roadName =
                                    findRoadName(
                                            impact
                                    );
                        }

                        if (roadName.isEmpty()) {

                            roadName =
                                    "Unknown road";
                        }

                        // -------------------------
                        // Type fallback
                        // -------------------------

                        if (type.isEmpty()) {

                            type =
                                    category;
                        }

                        // -------------------------
                        // Description fallback
                        // -------------------------

                        if (description.isEmpty()) {

                            description =
                                    type;
                        }

                        // -------------------------
                        // Single point location
                        // -------------------------

                        if (toLatitude == 0 &&
                                toLongitude == 0) {

                            toLatitude =
                                    fromLatitude;

                            toLongitude =
                                    fromLongitude;
                        }

                        Situation situation =
                                new Situation(
                                        id,
                                        category,
                                        type,
                                        description,
                                        roadName,
                                        impact,
                                        delayType,
                                        direction,
                                        startDate,
                                        endDate,
                                        fromLatitude,
                                        fromLongitude,
                                        toLatitude,
                                        toLongitude
                                );

                        situations.add(
                                situation
                        );

                        insideRecord =
                                false;
                    }

                    if (!tagStack.isEmpty()) {

                        tagStack.pop();
                    }
                }

                eventType =
                        parser.next();
            }

            Log.d(
                    "TrafficAPI",
                    category
                            + " situationRecord tags found: "
                            + recordsFound
            );

            Log.d(
                    "TrafficAPI",
                    category
                            + " objects created: "
                            + situations.size()
            );

        } catch (Exception e) {

            Log.e(
                    "TrafficAPI",
                    "XML parsing failed for "
                            + category,
                    e
            );
        }

        return situations;
    }

    // =========================================
    // ATTRIBUTE
    // =========================================

    private String findAttribute(
            XmlPullParser parser,
            String wantedName) {

        for (int i = 0;
             i < parser.getAttributeCount();
             i++) {

            String attributeName =
                    normaliseTag(
                            parser.getAttributeName(i)
                    );

            if (attributeName.equalsIgnoreCase(
                    wantedName)) {

                String value =
                        parser.getAttributeValue(i);

                if (value != null) {

                    return value.trim();
                }
            }
        }

        return "";
    }

    // =========================================
    // REMOVE PREFIX IF PRESENT
    // =========================================

    private String normaliseTag(
            String name) {

        if (name == null) {

            return "";
        }

        int colon =
                name.indexOf(':');

        if (colon >= 0 &&
                colon < name.length() - 1) {

            return name.substring(
                    colon + 1
            );
        }

        return name;
    }

    // =========================================
    // CLEAN xsi:type
    // =========================================

    private String cleanType(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "";
        }

        value =
                value.trim();

        int colon =
                value.indexOf(':');

        if (colon >= 0 &&
                colon < value.length() - 1) {

            value =
                    value.substring(
                            colon + 1
                    );
        }

        return makeReadable(
                value
        );
    }

    // =========================================
    // camelCase -> Camel Case
    // =========================================

    private String makeReadable(
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return "";
        }

        String readable =
                value.trim();

        int colon =
                readable.indexOf(':');

        if (colon >= 0 &&
                colon < readable.length() - 1) {

            readable =
                    readable.substring(
                            colon + 1
                    );
        }

        readable =
                readable.replace(
                        "_",
                        " "
                );

        readable =
                readable.replace(
                        "-",
                        " "
                );

        readable =
                readable.replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2"
                );

        readable =
                readable.trim();

        if (readable.isEmpty()) {

            return "";
        }

        return readable.substring(
                0,
                1
        ).toUpperCase(Locale.UK)
                + readable.substring(1);
    }

    // =========================================
    // APPEND DESCRIPTION TEXT
    // =========================================

    private void appendText(
            StringBuilder builder,
            String text) {

        if (text == null ||
                text.trim().isEmpty()) {

            return;
        }

        if (builder.length() > 0) {

            builder.append(
                    " "
            );
        }

        builder.append(
                text.trim()
        );
    }

    // =========================================
    // APPEND IMPACT
    // =========================================

    private void appendImpact(
            StringBuilder builder,
            String label,
            String value) {

        if (value == null ||
                value.trim().isEmpty()) {

            return;
        }

        String result =
                label
                        + ": "
                        + value.trim();

        // Avoid duplicates.
        if (builder
                .toString()
                .contains(result)) {

            return;
        }

        if (builder.length() > 0) {

            builder.append(
                    " | "
            );
        }

        builder.append(
                result
        );
    }

    // =========================================
    // DOUBLE
    // =========================================

    private Double parseDouble(
            String value) {

        try {

            return Double.parseDouble(
                    value.trim()
            );

        } catch (Exception e) {

            return null;
        }
    }

    // =========================================
    // FIND ROAD IN DESCRIPTION
    // =========================================

    private String findRoadName(
            String text) {

        if (text == null ||
                text.isEmpty()) {

            return "";
        }

        Pattern pattern =
                Pattern.compile(
                        "\\b(?:"
                                + "M\\d{1,3}"
                                + "|"
                                + "A\\d{1,4}(?:\\(M\\))?"
                                + "|"
                                + "B\\d{1,4}"
                                + ")\\b",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        text
                );

        if (matcher.find()) {

            return matcher
                    .group()
                    .toUpperCase(
                            Locale.UK
                    );
        }

        return "";
    }
}