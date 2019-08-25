package de.tum.`in`.tumcampusapp.component.tumui.roomfinder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.app.ApiHelper
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.generic.ImageViewTouchFragment
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground
import de.tum.`in`.tumcampusapp.component.other.locations.LocationManager
import de.tum.`in`.tumcampusapp.component.other.locations.model.Geo
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderCoordinate
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderMap
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.NetUtils
import de.tum.`in`.tumcampusapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

/**
 * Displays the map regarding the searched room.
 */
class RoomFinderDetailsActivity : ActivityForLoadingInBackground<Void, String>(R.layout.activity_roomfinderdetails), DialogInterface.OnClickListener {

    @Inject
    lateinit var tumCabeClient: TUMCabeClient

    private lateinit var imageFragment: ImageViewTouchFragment
    private var weekViewFragment: Fragment? = null

    private var mapsLoaded: Boolean = false

    private var mapId: String = ""
    private var mapsList: List<RoomFinderMap> = ArrayList()
    private val infoLoaded: Boolean = false

    private var roomFinderCoordinateCall: Call<RoomFinderCoordinate>? = null
    private var roomFinderMapsCall: Call<List<RoomFinderMap>>? = null

    private val room: RoomFinderRoom by lazy {
        intent.getSerializableExtra(EXTRA_ROOM_INFO) as RoomFinderRoom
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageFragment = ImageViewTouchFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, imageFragment)
                .commit()

        startLoading()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_roomfinder_detail, menu)

        val switchMap = menu.findItem(R.id.action_switch_map)
        switchMap.isVisible = "10" != mapId && mapsLoaded && weekViewFragment == null

        val timetable = menu.findItem(R.id.action_room_timetable)
        timetable.isVisible = infoLoaded
        timetable.setIcon(
                if (weekViewFragment == null) R.drawable.ic_outline_event_note_24px
                else R.drawable.ic_outline_map_24px
        )

        menu.findItem(R.id.action_directions).isVisible = infoLoaded && weekViewFragment == null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_room_timetable -> {
                toggleRoomTimetable()
                invalidateOptionsMenu()
                return true
            }
            R.id.action_directions -> {
                loadGeo()
                return true
            }
            R.id.action_switch_map -> {
                showMapSwitch()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Remove weekViewFragment with room timetable if present and show map again
        if (weekViewFragment != null) {
            toggleRoomTimetable()
            invalidateOptionsMenu()
            return
        }

        super.onBackPressed()
    }

    private fun toggleRoomTimetable() {
        val ft = supportFragmentManager.beginTransaction()
        // Remove if weekViewFragment is already present
        if (weekViewFragment != null) {
            ft.replace(R.id.fragment_container, imageFragment)
            ft.commit()
            weekViewFragment = null
            return
        }

        val roomApiCode = room.room_id
        weekViewFragment = WeekViewFragment.newInstance(roomApiCode)
        ft.replace(R.id.fragment_container, weekViewFragment!!)
        ft.commit()
    }

    private fun showMapSwitch() {
        val descriptions = mapsList.map { it.description }
        val currentPosition = mapsList.indexOfFirst { it.map_id == mapId }

        AlertDialog.Builder(this)
                .setSingleChoiceItems(descriptions.toTypedArray(), currentPosition, this)
                .show()
    }

    override fun onClick(dialog: DialogInterface, whichButton: Int) {
        dialog.dismiss()
        val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
        mapId = mapsList[selectedPosition].map_id
        startLoading()
    }

    override fun onLoadInBackground(vararg arg: Void): String? {
        val encodedArchId = ApiHelper.encodeUrl(room.arch_id)

        return if (mapId.isEmpty()) {
            Const.URL_DEFAULT_MAP_IMAGE + encodedArchId
        } else {
            Const.URL_MAP_IMAGE + encodedArchId + '/'.toString() + ApiHelper.encodeUrl(mapId)
        }
    }

    override fun onLoadFinished(url: String?) {
        if (url == null) {
            showImageLoadingError()
        } else {
            imageFragment.loadImage(url) { showImageLoadingError() }
        }

        supportActionBar?.title = room.info
        supportActionBar?.subtitle = room.formattedAddress

        showLoadingEnded()
        loadMapList()
    }

    private fun loadMapList() {
        showLoadingStart()

        roomFinderMapsCall = tumCabeClient.fetchAvailableMaps(room.arch_id)
        roomFinderMapsCall?.enqueue(object : Callback<List<RoomFinderMap>> {
            override fun onResponse(call: Call<List<RoomFinderMap>>,
                                    response: Response<List<RoomFinderMap>>) {
                val data = response.body()
                roomFinderMapsCall = null

                if (!response.isSuccessful || data == null) {
                    onMapListLoadFailed()
                    return
                }
                onMapListLoadFinished(data)
            }

            override fun onFailure(call: Call<List<RoomFinderMap>>,
                                   throwable: Throwable) {
                if (call.isCanceled) {
                    return
                }

                onMapListLoadFailed()
                roomFinderMapsCall = null
            }
        })
    }

    private fun onMapListLoadFailed() {
        onMapListLoadFinished(null)
    }

    private fun onMapListLoadFinished(result: List<RoomFinderMap>?) {
        showLoadingEnded()
        if (result == null) {
            if (NetUtils.isConnected(this)) {
                showErrorLayout()
            } else {
                showNoInternetLayout()
            }
            return
        }
        mapsList = result
        if (mapsList.size > 1) {
            mapsLoaded = true
        }

        invalidateOptionsMenu()
    }

    private fun loadGeo() {
        showLoadingStart()
        roomFinderCoordinateCall = tumCabeClient.fetchRoomFinderCoordinates(room.arch_id)
        roomFinderCoordinateCall?.enqueue(object : Callback<RoomFinderCoordinate> {
            override fun onResponse(call: Call<RoomFinderCoordinate>,
                                    response: Response<RoomFinderCoordinate>) {
                val data = response.body()
                roomFinderCoordinateCall = null

                if (!response.isSuccessful || data == null) {
                    onLoadGeoFailed()
                    return
                }

                onGeoLoadFinished(LocationManager.convertRoomFinderCoordinateToGeo(data))
            }

            override fun onFailure(call: Call<RoomFinderCoordinate>,
                                   throwable: Throwable) {
                if (call.isCanceled) {
                    return
                }

                onLoadGeoFailed()
                roomFinderCoordinateCall = null
            }
        })
    }

    private fun onLoadGeoFailed() {
        onGeoLoadFinished(null)
    }

    private fun onGeoLoadFinished(result: Geo?) {
        showLoadingEnded()
        if (result == null) {
            Utils.showToastOnUIThread(this@RoomFinderDetailsActivity, R.string.no_map_available)
            return
        }

        // Build get directions intent and see if some app can handle it
        val coordinates = "${result.latitude},${result.longitude}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$coordinates"))
        val pkgAppsList = applicationContext.packageManager
                .queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)

        // If some app can handle this intent start it
        if (pkgAppsList.isNotEmpty()) {
            startActivity(intent)
            return
        }

        // If no app is capable of opening it link to google maps market entry
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")))
        } catch (e: ActivityNotFoundException) {
            Utils.log(e)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.google.android.apps.maps")))
        }

    }

    private fun showImageLoadingError() {
        if (NetUtils.isConnected(this)) {
            showError(R.string.error_something_wrong)
        } else {
            showNoInternetLayout()
        }
    }

    override fun onStop() {
        super.onStop()
        roomFinderMapsCall?.cancel()
        roomFinderCoordinateCall?.cancel()
    }

    companion object {

        const val EXTRA_ROOM_INFO = "roomInfo"

        fun newIntent(
            context: Context,
            room: RoomFinderRoom
        ) = Intent(context, RoomFinderDetailsActivity::class.java).apply {
            putExtra(EXTRA_ROOM_INFO, room)
        }

    }
}
