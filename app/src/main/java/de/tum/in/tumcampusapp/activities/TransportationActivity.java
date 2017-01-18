package de.tum.in.tumcampusapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearchingInBackground;
import de.tum.in.tumcampusapp.adapters.NoResultsAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.MVVStationSuggestionProvider;
import de.tum.in.tumcampusapp.managers.RecentsManager;
import de.tum.in.tumcampusapp.managers.TransportManager;

/**
 * Activity to show transport stations and departures
 */
public class TransportationActivity extends ActivityForSearchingInBackground<Cursor> implements OnItemClickListener {

    private ListView listViewResults;
    private SimpleCursorAdapter adapterStations;
    private RecentsManager recentsManager;

    public TransportationActivity() {
        super(R.layout.activity_transportation, MVVStationSuggestionProvider.AUTHORITY, 3);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        recentsManager = new RecentsManager(this, RecentsManager.STATIONS);

        listViewResults = (ListView) findViewById(R.id.activity_transport_listview_result);
        listViewResults.setOnItemClickListener(this);

        // Initialize stations adapter
        Cursor stationCursor = recentsManager.getAllFromDb();
        adapterStations = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, stationCursor,
                stationCursor.getColumnNames(), new int[]{android.R.id.text1}, 0);

        if (adapterStations.getCount() == 0) {
            openSearch();
        } else {
            listViewResults.setAdapter(adapterStations);
            listViewResults.requestFocus();
        }
    }

    /**
     * Click on station in list
     */
    @Override
    public void onItemClick(final AdapterView<?> av, View v, int position, long id) {
        Cursor departureCursor = (Cursor) av.getAdapter().getItem(position);
        showStation(departureCursor.getString(departureCursor.getColumnIndex(Const.NAME_COLUMN)),
                departureCursor.getString(departureCursor.getColumnIndex(Const.ID_COLUMN)));
    }

    /**
     * Opens {@link TransportationDetailsActivity} with departure times for the specified station
     *
     * @param station Station
     */
    void showStation(String station, String stationID) {
        Intent intent = new Intent(this, TransportationDetailsActivity.class);
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION, station);
        intent.putExtra(TransportationDetailsActivity.EXTRA_STATION_ID, stationID);
        startActivity(intent);
    }


    /**
     * Shows all recently used stations
     *
     * @return Cursor holding the recents information (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground() {
        return Optional.of(recentsManager.getAllFromDb());
    }

    /**
     * Searches the Webservice for stations
     *
     * @param query the text entered by the user
     * @return Cursor holding the stations (name, _id)
     */
    @Override
    public Optional<Cursor> onSearchInBackground(String query) {
        // Get Information
        Optional<Cursor> stationCursor = TransportManager.getStationsFromExternal(this, query);
        if (!stationCursor.isPresent()) {
            showError(R.string.exception_unknown);
        }

        // Drop results if canceled
        if (asyncTask.isCancelled()) {
            return Optional.absent();
        }

        return stationCursor;
    }

    /**
     * Shows the stations
     *
     * @param possibleStationCursor Cursor with stations (name, _id)
     */
    @Override
    protected void onSearchFinished(Optional<Cursor> possibleStationCursor) {
        if (!possibleStationCursor.isPresent()) {
            return;
        }
        Cursor stationCursor = possibleStationCursor.get();

        showLoadingEnded();

        // mQuery is not null if it was a real search
        // If there is exactly one station, open results directly
        if (stationCursor.getCount() == 1 && mQuery != null) {
            stationCursor.moveToFirst();
            showStation(stationCursor.getString(0), stationCursor.getString(1));
            return;
        } else if (stationCursor.getCount() == 0) {
            // When stationCursor is a MatrixCursor the result comes from querying a station name
            if (stationCursor instanceof MatrixCursor) {
                // So show no results found
                listViewResults.setAdapter(new NoResultsAdapter(this));
                listViewResults.requestFocus();
            } else {
                // if the loading came from the user canceling search
                // and there are no recents to show close activity
                finish();
            }
            return;
        }

        adapterStations.changeCursor(stationCursor);
        listViewResults.setAdapter(adapterStations);
        listViewResults.requestFocus();
    }
}