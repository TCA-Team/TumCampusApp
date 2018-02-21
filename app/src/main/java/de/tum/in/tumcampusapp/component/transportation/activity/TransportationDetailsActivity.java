package de.tum.in.tumcampusapp.component.transportation.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.general.RecentsDao;
import de.tum.in.tumcampusapp.component.general.model.Recent;
import de.tum.in.tumcampusapp.component.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.transportation.DepartureView;
import de.tum.in.tumcampusapp.component.transportation.controller.TransportManager;
import de.tum.in.tumcampusapp.component.transportation.model.efa.Departure;
import de.tum.in.tumcampusapp.component.transportation.model.efa.StationResult;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.NetUtils;

/**
 * Activity to show transport departures for a specified station
 * <p>
 * NEEDS: EXTRA_STATION set in incoming bundle (station name)
 */
public class TransportationDetailsActivity extends ActivityForLoadingInBackground<String, List<Departure>> {
    public static final String EXTRA_STATION = "station";
    public static final String EXTRA_STATION_ID = "stationID";

    private LinearLayout mViewResults;
    private RecentsDao recentsDao;
    private TransportManager transportManager;
    private Gson gson;

    public TransportationDetailsActivity() {
        super(R.layout.activity_transportation_detail);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get all stations from db
        recentsDao = TcaDb.getInstance(this)
                          .recentsDao();
        transportManager = new TransportManager(this);
        gson = new Gson();
        mViewResults = this.findViewById(R.id.activity_transport_result);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String location = intent.getStringExtra(EXTRA_STATION);
        setTitle(location);
        String locationID = intent.getStringExtra(EXTRA_STATION_ID);

        startLoading(location, locationID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transport, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_transport_usage) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.transport_action_usage)
                    .setMessage(R.string.transport_help_text)
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load departure times
     *
     * @param arg Station name
     * @return List of departures
     */
    @Override
    protected List<Departure> onLoadInBackground(String... arg) {
        final String location = arg[0];
        final String locationID = arg[1];
        final StationResult stationResult = new StationResult(location, locationID, Integer.MAX_VALUE); // Quality is always 100% hit
        final String jsonStationResult = gson.toJson(stationResult);

        // save clicked station into db
        recentsDao.insert(new Recent(jsonStationResult, RecentsDao.STATIONS));

        // Check for internet connectivity
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return null;
        }

        // get departures from website
        List<Departure> departures = TransportManager.getDeparturesFromExternal(this, locationID);
        if (departures.isEmpty()) {
            showError(R.string.no_departures_found);
        }

        return departures;
    }

    /**
     * Adds a new {@link DepartureView} for each departure entry
     *
     * @param result List of departures
     */
    @Override
    protected void onLoadFinished(List<Departure> result) {
        showLoadingEnded();
        if (result == null) {
            return;
        }
        mViewResults.removeAllViews();
        for (Departure d : result) {
            DepartureView view = new DepartureView(this, true);

            view.setOnClickListener(v -> {
                DepartureView departureView = (DepartureView) v;
                String symbol = departureView.getSymbol();
                boolean highlight;
                if (transportManager.isFavorite(symbol)) {
                    transportManager.deleteFavorite(symbol);
                    highlight = false;
                } else {
                    transportManager.addFavorite(symbol);
                    highlight = true;
                }

                // Update the other views with the same symbol
                for (int i = 0; i < mViewResults.getChildCount(); i++) {
                    DepartureView child = (DepartureView) mViewResults.getChildAt(i);
                    if (child.getSymbol()
                             .equals(symbol)) {
                        child.setSymbol(symbol, highlight);
                    }
                }
            });

            if (transportManager.isFavorite(d.getSymbol())) {
                view.setSymbol(d.getSymbol(), true);
            } else {
                view.setSymbol(d.getSymbol(), false);
            }

            view.setLine(d.getDirection());
            view.setTime(d.getDepartureTime());
            mViewResults.addView(view);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < mViewResults.getChildCount(); i++) {
            View view = mViewResults.getChildAt(i);
            if (!(view instanceof DepartureView)) {
                continue;
            }
            ((DepartureView) view).removeAllCallbacksAndMessages();
        }
    }
}