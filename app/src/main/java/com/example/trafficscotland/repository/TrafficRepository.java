package com.example.trafficscotland.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.trafficscotland.model.Situation;
import com.example.trafficscotland.parser.TrafficXmlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrafficRepository {

    private static final String TAG = "TrafficAPI";

    private static final String UNPLANNED_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";

    private static final String ROADWORKS_URL =
            "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    /*
     * Cache files kept inside the app's
     * private internal storage.
     */
    private static final String INCIDENT_CACHE_FILE =
            "cached_unplanned_events.xml";

    private static final String ROADWORK_CACHE_FILE =
            "cached_current_roadworks.xml";

    private static final String CACHE_PREFERENCES =
            "traffic_cache_preferences";

    private static final String CACHE_TIME_KEY =
            "last_successful_cache_time";

    private static TrafficRepository instance;

    private final Context appContext;

    private final ExecutorService executorService;

    private final TrafficApiClient apiClient;

    private final TrafficXmlParser parser;

    private volatile boolean lastLoadFromCache = false;

    private TrafficRepository(Context context) {

        appContext =
                context.getApplicationContext();

        executorService =
                Executors.newSingleThreadExecutor();

        apiClient =
                new TrafficApiClient();

        parser =
                new TrafficXmlParser();
    }

    public static synchronized TrafficRepository getInstance(
            Context context) {

        if (instance == null) {

            instance =
                    new TrafficRepository(
                            context
                    );
        }

        return instance;
    }

    public interface TrafficDataCallback {

        void onSuccess(
                List<Situation> situations
        );

        void onError(
                String error
        );
    }

    public void loadTrafficData(
            TrafficDataCallback callback) {

        executorService.execute(() -> {

            Log.d(
                    TAG,
                    "Loading Traffic Scotland data..."
            );

            try {

                /*
                 * =========================
                 * TRY LIVE DATA FIRST
                 * =========================
                 */

                Log.d(
                        TAG,
                        "Downloading incidents..."
                );

                String incidentXml =
                        apiClient.downloadXml(
                                UNPLANNED_URL
                        );

                Log.d(
                        TAG,
                        "Parsing incidents..."
                );

                List<Situation> incidents =
                        parser.parse(
                                incidentXml,
                                "Incident"
                        );

                Log.d(
                        TAG,
                        "Parsed incidents: "
                                + incidents.size()
                );

                Log.d(
                        TAG,
                        "Downloading roadworks..."
                );

                String roadworkXml =
                        apiClient.downloadXml(
                                ROADWORKS_URL
                        );

                Log.d(
                        TAG,
                        "Parsing roadworks..."
                );

                List<Situation> roadworks =
                        parser.parse(
                                roadworkXml,
                                "Roadwork"
                        );

                Log.d(
                        TAG,
                        "Parsed roadworks: "
                                + roadworks.size()
                );

                /*
                 * Only save the cache once BOTH
                 * feeds have downloaded and parsed
                 * successfully.
                 *
                 * This stops bad/partial downloads
                 * replacing the last working cache.
                 */
                saveCache(
                        incidentXml,
                        roadworkXml
                );

                ArrayList<Situation> allSituations =
                        new ArrayList<>();

                allSituations.addAll(
                        incidents
                );

                allSituations.addAll(
                        roadworks
                );

                lastLoadFromCache = false;

                Log.d(
                        TAG,
                        "Total live situations: "
                                + allSituations.size()
                );

                callback.onSuccess(
                        allSituations
                );

            } catch (Exception liveException) {

                Log.w(
                        TAG,
                        "Live download failed. Trying cached XML...",
                        liveException
                );

                /*
                 * =========================
                 * LIVE DOWNLOAD FAILED
                 * TRY LAST SUCCESSFUL CACHE
                 * =========================
                 */

                try {

                    List<Situation> cachedSituations =
                            loadCachedTrafficData();

                    lastLoadFromCache = true;

                    Log.d(
                            TAG,
                            "Loaded "
                                    + cachedSituations.size()
                                    + " situations from cache"
                    );

                    callback.onSuccess(
                            cachedSituations
                    );

                } catch (Exception cacheException) {

                    Log.e(
                            TAG,
                            "No usable cached traffic data",
                            cacheException
                    );

                    lastLoadFromCache = false;

                    callback.onError(
                            "Unable to download Traffic Scotland data "
                                    + "and no saved offline data is available."
                    );
                }
            }
        });
    }

    private void saveCache(
            String incidentXml,
            String roadworkXml)
            throws Exception {

        if (incidentXml == null
                || incidentXml.trim().isEmpty()
                || roadworkXml == null
                || roadworkXml.trim().isEmpty()) {

            throw new Exception(
                    "Cannot save empty traffic data"
            );
        }

        writeFile(
                INCIDENT_CACHE_FILE,
                incidentXml
        );

        writeFile(
                ROADWORK_CACHE_FILE,
                roadworkXml
        );

        long now =
                System.currentTimeMillis();

        SharedPreferences preferences =
                appContext.getSharedPreferences(
                        CACHE_PREFERENCES,
                        Context.MODE_PRIVATE
                );

        preferences.edit()
                .putLong(
                        CACHE_TIME_KEY,
                        now
                )
                .apply();

        Log.d(
                TAG,
                "Saved latest successful XML cache"
        );
    }

    private List<Situation>
    loadCachedTrafficData()
            throws Exception {

        if (!hasCachedData()) {

            throw new Exception(
                    "Cache files do not exist"
            );
        }

        String incidentXml =
                readFile(
                        INCIDENT_CACHE_FILE
                );

        String roadworkXml =
                readFile(
                        ROADWORK_CACHE_FILE
                );

        if (incidentXml.trim().isEmpty()
                || roadworkXml.trim().isEmpty()) {

            throw new Exception(
                    "Cached XML is empty"
            );
        }

        Log.d(
                TAG,
                "Parsing cached incidents..."
        );

        List<Situation> incidents =
                parser.parse(
                        incidentXml,
                        "Incident"
                );

        Log.d(
                TAG,
                "Parsing cached roadworks..."
        );

        List<Situation> roadworks =
                parser.parse(
                        roadworkXml,
                        "Roadwork"
                );

        ArrayList<Situation> allSituations =
                new ArrayList<>();

        allSituations.addAll(
                incidents
        );

        allSituations.addAll(
                roadworks
        );

        return allSituations;
    }

    private void writeFile(
            String fileName,
            String contents)
            throws Exception {

        try (
                FileOutputStream outputStream =
                        appContext.openFileOutput(
                                fileName,
                                Context.MODE_PRIVATE
                        );

                OutputStreamWriter writer =
                        new OutputStreamWriter(
                                outputStream,
                                StandardCharsets.UTF_8
                        )
        ) {

            writer.write(
                    contents
            );

            writer.flush();
        }
    }

    private String readFile(
            String fileName)
            throws Exception {

        StringBuilder builder =
                new StringBuilder();

        try (
                FileInputStream inputStream =
                        appContext.openFileInput(
                                fileName
                        );

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line =
                    reader.readLine()) != null) {

                builder.append(
                        line
                );

                builder.append(
                        '\n'
                );
            }
        }

        return builder.toString();
    }

    public boolean hasCachedData() {

        File incidentFile =
                new File(
                        appContext.getFilesDir(),
                        INCIDENT_CACHE_FILE
                );

        File roadworkFile =
                new File(
                        appContext.getFilesDir(),
                        ROADWORK_CACHE_FILE
                );

        return incidentFile.exists()
                && roadworkFile.exists()
                && incidentFile.length() > 0
                && roadworkFile.length() > 0;
    }

    public boolean wasLastLoadFromCache() {

        return lastLoadFromCache;
    }

    public long getLastCacheTimestamp() {

        SharedPreferences preferences =
                appContext.getSharedPreferences(
                        CACHE_PREFERENCES,
                        Context.MODE_PRIVATE
                );

        return preferences.getLong(
                CACHE_TIME_KEY,
                0
        );
    }
}