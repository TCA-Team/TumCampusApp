package de.tum.`in`.tumcampusapp.component.ui.barrierfree

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom

class RoomsAdapter(
    context: Context,
    items: List<RoomFinderRoom>
) : SimpleStickyListHeadersAdapter<RoomFinderRoom>(context, items.toMutableList()) {

    internal class ViewHolder {
        var tvRoomTitle: TextView? = null
        var tvBuildingTitle: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_roomfinder_item, parent, false)
            holder = ViewHolder()
            holder.tvRoomTitle = view.findViewById(R.id.startup_actionbar_title)
            holder.tvBuildingTitle = view.findViewById(R.id.building)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val room = itemList[position]
        // Setting all values in listView
        holder.tvRoomTitle?.text = room.info
        holder.tvBuildingTitle?.text = room.formattedAddress
        return view
    }
}
