package de.tum.in.tumcampusapp.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.ActivityForSearching;
import de.tum.in.tumcampusapp.adapters.RoomFinderListAdapter;
import de.tum.in.tumcampusapp.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampusapp.auxiliary.RoomFinderSuggestionProvider;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequest;
import de.tum.in.tumcampusapp.tumonline.TUMRoomFinderRequestFetchListener;

/**
 * Activity to show a convenience interface for using the MyTUM room finder.
 * 
 * @author Vincenz Doelle, Anas Chackfeh
 */
public class RoomfinderActivity extends ActivityForSearching implements OnEditorActionListener, TUMRoomFinderRequestFetchListener, OnItemClickListener,
		TextWatcher {

	// HTTP client for sending requests to MyTUM roomfinder
	TUMRoomFinderRequest roomFinderRequest;

	ListView list;
	RoomFinderListAdapter adapter;

	String currentlySelectedBuildingId;

	private String currentlySelectedRoomId;
	private SharedPreferences sharedPrefs;

	public RoomfinderActivity() {
		super(R.layout.activity_roomfinder);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	private void doSearch(String query) {

		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RoomFinderSuggestionProvider.AUTHORITY, RoomFinderSuggestionProvider.MODE);
		suggestions.saveRecentQuery(query, null);

		this.roomFinderRequest.fetchSearchInteractive(this, this, query);

	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			this.doSearch(query);
		}
	}

	/**
	 * Exported, because unused.
	 * 
	 * @param s
	 */
	@SuppressWarnings("unused")
	private void initQuerySearchView(CharSequence s) {
		final int SEARCHVIEW_FIELD_ID = 0;
		SearchView searchView = (SearchView) this.findViewById(SEARCHVIEW_FIELD_ID);
		searchView.setQuery(s, false);
	}

	/**
	 * Exported, because unused.
	 */
	@SuppressWarnings("unused")
	private void initSearchView() {
		final int SEARCHVIEW_FIELD_ID = 0;
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
		this.searchField.addTextChangedListener(this);

		SearchView searchView = (SearchView) this.findViewById(SEARCHVIEW_FIELD_ID);
		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
		searchView.setIconifiedByDefault(false);
		// Do not iconify the widget;
		// expand it by default
		searchView.getRootView().requestFocus();

		Intent intent = this.getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			this.doSearch(query);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
	}

	@Override
	public void onCommonError(String errorReason) {
		Toast.makeText(this, errorReason, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.roomFinderRequest = new TUMRoomFinderRequest();
		// Counting the number of times that the user used this activity for intelligent reordering
		this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (this.sharedPrefs.getBoolean("implicitly_id", true)) {
			ImplicitCounter.Counter("roomfinder_id", this.getApplicationContext());
		}
		Bundle bunble = this.getIntent().getExtras();

		if (bunble != null) {

			// Getting the value stored in the name "NAME" (binding the calender and room finder)
			String searchQuery = bunble.getString("NAME");
			this.doSearch(searchQuery);

		}

	}

	@Override
	public void onFetch(ArrayList<HashMap<String, String>> result) {
		this.list = (ListView) this.findViewById(R.id.list);

		// Getting adapter by passing xml data ArrayList
		this.adapter = new RoomFinderListAdapter(this, result);
		this.list.setAdapter(this.adapter);

		// Click event for single list row
		this.list.setOnItemClickListener(this);

		this.progressLayout.setVisibility(View.GONE);
		if (result.size() == 0) {
			Toast.makeText(this, R.string.no_rooms_found, Toast.LENGTH_SHORT).show();
			this.errorLayout.setVisibility(View.VISIBLE);
			return;
		}
		this.errorLayout.setVisibility(View.GONE);
	}

	@Override
	public void onFetchCancelled() {
		this.onFetchError("");
	}

	@Override
	public void onFetchDefaultMapId(String mapId) {
		Intent intent = new Intent(this, RoomFinderDetailsActivity.class);
		intent.putExtra("buildingId", this.currentlySelectedBuildingId);
		intent.putExtra("roomId", this.currentlySelectedRoomId);
		intent.putExtra("mapId", mapId);

		this.startActivity(intent);
	}

	@Override
	public void onFetchError(String errorReason) {
		this.roomFinderRequest.cancelRequest(true);
		this.errorLayout.setVisibility(View.VISIBLE);
		this.progressLayout.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		@SuppressWarnings("unchecked")
		HashMap<String, String> room = (HashMap<String, String>) this.list.getAdapter().getItem(position);

		this.currentlySelectedBuildingId = room.get(TUMRoomFinderRequest.KEY_Building + TUMRoomFinderRequest.KEY_ID);

		this.currentlySelectedRoomId = room.get(TUMRoomFinderRequest.KEY_ARCHITECT_NUMBER);

		this.roomFinderRequest.fetchDefaultMapIdJob(this, this, this.currentlySelectedBuildingId);

	}

	@Override
	public void onNewIntent(Intent intent) {
		this.setIntent(intent);
		this.handleIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setDrawableColorForId(this, R.drawable.about);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public boolean performSearchAlgorithm() {
		EditText searchString = (EditText) this.findViewById(R.id.search_field);

		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, RoomFinderSuggestionProvider.AUTHORITY, RoomFinderSuggestionProvider.MODE);
		suggestions.saveRecentQuery(searchString.getText().toString(), null);

		this.roomFinderRequest.fetchSearchInteractive(this, this, searchString.getText().toString());
		return true;
	}
}