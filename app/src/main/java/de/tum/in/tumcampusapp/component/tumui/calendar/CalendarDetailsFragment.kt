package de.tum.`in`.tumcampusapp.component.tumui.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.transaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.api.tumonline.exception.RequestLimitReachedException
import de.tum.`in`.tumcampusapp.component.other.navigation.NavDestination
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.CalendarItem
import de.tum.`in`.tumcampusapp.component.tumui.calendar.model.DeleteEventResponse
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Const.CALENDAR_ID_PARAM
import de.tum.`in`.tumcampusapp.utils.Const.CALENDAR_SHOWN_IN_CALENDAR_ACTIVITY_PARAM
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.ui.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_calendar_details.buttonsContainer
import kotlinx.android.synthetic.main.fragment_calendar_details.cancelButtonsContainer
import kotlinx.android.synthetic.main.fragment_calendar_details.dateTextView
import kotlinx.android.synthetic.main.fragment_calendar_details.deleteButton
import kotlinx.android.synthetic.main.fragment_calendar_details.descriptionTextView
import kotlinx.android.synthetic.main.fragment_calendar_details.editButton
import kotlinx.android.synthetic.main.fragment_calendar_details.locationIcon
import kotlinx.android.synthetic.main.fragment_calendar_details.locationLinearLayout
import kotlinx.android.synthetic.main.fragment_calendar_details.showInCalendarButton
import kotlinx.android.synthetic.main.fragment_calendar_details.showInCalendarButtonContainer
import kotlinx.android.synthetic.main.fragment_calendar_details.titleTextView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException

class CalendarDetailsFragment : RoundedBottomSheetDialogFragment() {

    private var listener: OnEventInteractionListener? = null

    private val calendarItemId: String by lazy {
        arguments?.getString(CALENDAR_ID_PARAM) ?: throw IllegalStateException("No calendar item ID passed")
    }

    private val isShownInCalendarActivity: Boolean by lazy {
        arguments?.getBoolean(CALENDAR_SHOWN_IN_CALENDAR_ACTIVITY_PARAM)
                ?: throw IllegalStateException("Incomplete Bundle when opening calendar details fragment")
    }

    private var deleteApiCall: Call<DeleteEventResponse>? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.let {
            val dao = TcaDb.getInstance(it).calendarDao()
            val calendarItem = dao.getCalendarItemsById(calendarItemId)
            updateView(calendarItem)
        }
    }

    private fun updateView(calendarItemList: List<CalendarItem>) {
        val calendarItem = calendarItemList[0]

        if (calendarItemList.all { it.isCanceled }) {
            cancelButtonsContainer.visibility = View.VISIBLE
            descriptionTextView.setTextColor(Color.RED)
        }

        titleTextView.text = calendarItem.getFormattedTitle()
        dateTextView.text = calendarItem.getEventDateString()

        val locationList = calendarItemList.map { it.location }
        if (locationList.all { it.isBlank() }) {
            locationIcon.visibility = View.GONE
        } else {
            locationIcon.visibility = View.VISIBLE
            for (item in calendarItemList) {
                if (item.location.isBlank()) {
                    continue
                }
                val locationText: TextView = layoutInflater
                        .inflate(R.layout.calendar_location_text, locationLinearLayout, false) as TextView
                if (item.isCanceled) {
                    locationText.setTextColor(ContextCompat.getColor(requireContext(), R.color.event_canceled))
                    val textForCancelledEvent = "${item.location} (${R.string.event_canceled})"
                    locationText.text = textForCancelledEvent
                } else {
                    locationText.text = item.location
                }
                locationText.setOnClickListener { onLocationClicked(item.location) }
                locationLinearLayout.addView(locationText)
            }
        }

        if (calendarItem.description.isEmpty()) {
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.text = calendarItem.description
        }

        if (!isShownInCalendarActivity) {
            showInCalendarButtonContainer.visibility = View.VISIBLE
            showInCalendarButton.setOnClickListener { openEventInCalendarActivity(calendarItem) }
        }

        if (calendarItem.isEditable && isShownInCalendarActivity) {
            // We only provide edit and delete functionality if the user is in CalendarActivity,
            // but not if the user opens the fragment from MainActivity.
            buttonsContainer.visibility = View.VISIBLE
            deleteButton.setOnClickListener { displayDeleteDialog(calendarItem.nr) }
            editButton.setOnClickListener { listener?.onEditEvent(calendarItem) }
        } else {
            buttonsContainer.visibility = View.GONE
        }
    }

    private fun openEventInCalendarActivity(calendarItem: CalendarItem) {
        val args = Bundle().apply { putLong(Const.EVENT_TIME, calendarItem.eventStart.millis) }
        val destination = NavDestination.Fragment(CalendarFragment::class.java, args)
        NavigationManager.open(requireContext(), destination)
    }

    private fun displayDeleteDialog(eventId: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.event_delete_title)
                .setMessage(R.string.delete_event_info)
                .setPositiveButton(R.string.delete) { _, _ -> deleteEvent(eventId) }
                .setNegativeButton(R.string.cancel, null)
                .create()

        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_corners_background)
        alertDialog.show()
    }

    private fun deleteEvent(eventId: String) {
        val c = requireContext()
        deleteApiCall = TUMOnlineClient.getInstance(c).deleteEvent(eventId)
        deleteApiCall?.enqueue(object : Callback<DeleteEventResponse> {
            override fun onResponse(call: Call<DeleteEventResponse>,
                                    response: Response<DeleteEventResponse>) {
                dismiss()
                listener?.onEventDeleted(eventId)
                deleteApiCall = null
            }

            override fun onFailure(call: Call<DeleteEventResponse>, t: Throwable) {
                if (call.isCanceled) {
                    return
                }
                deleteApiCall = null
                handleDeleteEventError(t)
            }
        })
    }

    private fun handleDeleteEventError(t: Throwable) {
        val c = requireContext()
        val messageResId = when (t) {
            is UnknownHostException -> R.string.error_no_internet_connection
            is RequestLimitReachedException -> R.string.error_request_limit_reached
            else -> R.string.error_unknown
        }
        Utils.showToast(c, messageResId)
    }

    private fun onLocationClicked(location: String) {
        val query = Utils.extractRoomNumberFromLocation(location)
        val fragment = SearchFragment.newInstance(query)
        requireFragmentManager().transaction {
            replace(R.id.contentFrame, fragment)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(calendarItemId: String,
                        isShownInCalendarActivity: Boolean = true,
                        listener: OnEventInteractionListener? = null): CalendarDetailsFragment {
            return CalendarDetailsFragment().apply {
                this.arguments = Bundle().apply {
                    putString(CALENDAR_ID_PARAM, calendarItemId)
                    putBoolean(CALENDAR_SHOWN_IN_CALENDAR_ACTIVITY_PARAM, isShownInCalendarActivity)
                }
                this.listener = listener
            }
        }

    }

    override fun onDetach() {
        super.onDetach()
        deleteApiCall?.cancel()
    }

    interface OnEventInteractionListener {
        fun onEventDeleted(eventId: String)
        fun onEditEvent(calendarItem: CalendarItem)
    }

}