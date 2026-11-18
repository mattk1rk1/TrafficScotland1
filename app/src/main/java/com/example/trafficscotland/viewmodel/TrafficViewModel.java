package com.example.trafficscotland.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.trafficscotland.model.Situation;
import com.example.trafficscotland.repository.TrafficRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrafficViewModel
        extends AndroidViewModel {

    private static final String TAG =
            "TrafficAPI";

    /*
     * Production refresh interval:
     * one hour.
     */
    private static final long REFRESH_INTERVAL_MS =
            60 * 60 * 1000L;

    private final MutableLiveData<List<Situation>>
            situations;

    private final MutableLiveData<Situation>
            selectedSituation;

    private final MutableLiveData<List<Situation>>
            selectedMapSituations;

    private final MutableLiveData<Boolean>
            loading;

    private final MutableLiveData<String>
            errorMessage;

    private final MutableLiveData<String>
            lastUpdated;

    /*
     * Lets us know whether the currently
     * displayed data came from the live API
     * or the saved offline cache.
     */
    private final MutableLiveData<String>
            dataStatus;

    private final TrafficRepository repository;

    private final Handler refreshHandler;

    private volatile boolean refreshInProgress =
            false;

    private final Runnable automaticRefreshRunnable =
            new Runnable() {

                @Override
                public void run() {

                    Log.d(
                            TAG,
                            "Automatic hourly refresh triggered"
                    );

                    refreshData();

                    refreshHandler.postDelayed(
                            this,
                            REFRESH_INTERVAL_MS
                    );
                }
            };

    public TrafficViewModel(
            @NonNull Application application) {

        super(
                application
        );

        Log.d(
                TAG,
                "TrafficViewModel created"
        );

        situations =
                new MutableLiveData<>(
                        new ArrayList<>()
                );

        selectedSituation =
                new MutableLiveData<>();

        selectedMapSituations =
                new MutableLiveData<>(
                        new ArrayList<>()
                );

        loading =
                new MutableLiveData<>(
                        false
                );

        errorMessage =
                new MutableLiveData<>(
                        ""
                );

        lastUpdated =
                new MutableLiveData<>(
                        "Not updated yet"
                );

        dataStatus =
                new MutableLiveData<>(
                        "Loading traffic data..."
                );

        repository =
                TrafficRepository.getInstance(
                        application
                );

        refreshHandler =
                new Handler(
                        Looper.getMainLooper()
                );

        /*
         * Load immediately when the
         * application starts.
         */
        refreshData();

        /*
         * Then continue updating hourly.
         */
        startAutomaticRefresh();
    }

    public void refreshData() {

        if (refreshInProgress) {

            Log.d(
                    TAG,
                    "Refresh already running - request ignored"
            );

            return;
        }

        refreshInProgress =
                true;

        Log.d(
                TAG,
                "refreshData called"
        );

        loading.setValue(
                true
        );

        errorMessage.setValue(
                ""
        );

        dataStatus.setValue(
                "Updating Traffic Scotland data..."
        );

        repository.loadTrafficData(
                new TrafficRepository
                        .TrafficDataCallback() {

                    @Override
                    public void onSuccess(
                            List<Situation>
                                    newSituations) {

                        situations.postValue(
                                newSituations
                        );

                        loading.postValue(
                                false
                        );

                        errorMessage.postValue(
                                ""
                        );

                        if (repository
                                .wasLastLoadFromCache()) {

                            /*
                             * Network failed but repository
                             * successfully loaded the last
                             * saved XML.
                             */
                            long cacheTime =
                                    repository
                                            .getLastCacheTimestamp();

                            String cachedAt =
                                    formatCacheTime(
                                            cacheTime
                                    );

                            dataStatus.postValue(
                                    "Offline - showing saved data from "
                                            + cachedAt
                            );

                            lastUpdated.postValue(
                                    cachedAt
                            );

                            Log.d(
                                    TAG,
                                    "ViewModel using OFFLINE cached data"
                            );

                        } else {

                            /*
                             * Successful fresh download.
                             */
                            String updateTime =
                                    LocalDateTime.now()
                                            .format(
                                                    DateTimeFormatter
                                                            .ofPattern(
                                                                    "dd/MM/yyyy HH:mm"
                                                            )
                                            );

                            dataStatus.postValue(
                                    "Live Traffic Scotland data"
                            );

                            lastUpdated.postValue(
                                    updateTime
                            );

                            Log.d(
                                    TAG,
                                    "ViewModel using LIVE data"
                            );
                        }

                        Log.d(
                                TAG,
                                "ViewModel received "
                                        + newSituations.size()
                                        + " situations"
                        );

                        refreshInProgress =
                                false;
                    }

                    @Override
                    public void onError(
                            String error) {

                        Log.e(
                                TAG,
                                "ViewModel received error: "
                                        + error
                        );

                        errorMessage.postValue(
                                error
                        );

                        dataStatus.postValue(
                                "No traffic data available"
                        );

                        loading.postValue(
                                false
                        );

                        refreshInProgress =
                                false;
                    }
                }
        );
    }

    private void startAutomaticRefresh() {

        refreshHandler.removeCallbacks(
                automaticRefreshRunnable
        );

        refreshHandler.postDelayed(
                automaticRefreshRunnable,
                REFRESH_INTERVAL_MS
        );

        Log.d(
                TAG,
                "Automatic refresh scheduled every 60 minutes"
        );
    }

    private String formatCacheTime(
            long timestamp) {

        if (timestamp == 0) {

            return "previous successful update";
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.UK
                );

        return formatter.format(
                new Date(
                        timestamp
                )
        );
    }

    @Override
    protected void onCleared() {

        super.onCleared();

        refreshHandler.removeCallbacks(
                automaticRefreshRunnable
        );

        Log.d(
                TAG,
                "Automatic refresh stopped"
        );
    }

    public LiveData<List<Situation>>
    getSituations() {

        return situations;
    }

    public LiveData<Situation>
    getSelectedSituation() {

        return selectedSituation;
    }

    public LiveData<List<Situation>>
    getSelectedMapSituations() {

        return selectedMapSituations;
    }

    public LiveData<Boolean>
    getLoading() {

        return loading;
    }

    public LiveData<String>
    getErrorMessage() {

        return errorMessage;
    }

    public LiveData<String>
    getLastUpdated() {

        return lastUpdated;
    }

    public LiveData<String>
    getDataStatus() {

        return dataStatus;
    }

    public void selectSituation(
            Situation situation) {

        selectedSituation.setValue(
                situation
        );
    }

    public void setSelectedMapSituations(
            List<Situation> selected) {

        selectedMapSituations.setValue(
                new ArrayList<>(
                        selected
                )
        );
    }

    public void clearSelectedMapSituations() {

        selectedMapSituations.setValue(
                new ArrayList<>()
        );
    }
}