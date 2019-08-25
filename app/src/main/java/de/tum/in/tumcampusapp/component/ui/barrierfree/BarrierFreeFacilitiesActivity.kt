package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.general.RecentsDao
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.database.TcaDb
import kotlinx.android.synthetic.main.activity_barrier_free_facilities.barrierFreeFacilitiesListView
import kotlinx.android.synthetic.main.activity_barrier_free_facilities.spinnerToolbar
import retrofit2.Call

class BarrierFreeFacilitiesActivity : ActivityForAccessingTumCabe<List<RoomFinderRoom>>(
        R.layout.activity_barrier_free_facilities
), AdapterView.OnItemSelectedListener {

    private val recents: RecentsDao by lazy {
        TcaDb.getInstance(this).recentsDao()
    }

    private val locationManager: LocationManager by lazy {
        LocationManager(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        spinnerToolbar.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (position) {
            0 -> fetchApiCallForCurrentLocation()
            1 -> executeApiCall(apiClient.listOfElevators)
            else -> executeApiCall(apiClient.listOfElevators)
        }
    }

    private fun fetchApiCallForCurrentLocation() {
        locationManager.fetchBuildingIDFromCurrentLocation {
            val apiCall = apiClient.getListOfNearbyFacilities(it)
            executeApiCall(apiCall)
        }
    }

    private fun executeApiCall(apiCall: Call<List<RoomFinderRoom>>?) {
        apiCall?.let { fetch(it) } ?: showError(R.string.error_something_wrong)
    }

    override fun onDownloadSuccessful(response: List<RoomFinderRoom>) {
        barrierFreeFacilitiesListView.adapter = RoomsAdapter(this, response)
        barrierFreeFacilitiesListView.setOnItemClickListener { _, _, index, _ ->
            val facility = response[index]
            recents.insert(Recent(facility.toString(), RecentsDao.ROOMS))
            openRoomFinderDetails(facility)
        }
    }

    private fun openRoomFinderDetails(facility: RoomFinderRoom) {
        val intent = Intent(this, RoomFinderDetailsActivity::class.java)
        intent.putExtra(RoomFinderDetailsActivity.EXTRA_ROOM_INFO, facility)
        startActivity(intent)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Nothing selected
    }

}
