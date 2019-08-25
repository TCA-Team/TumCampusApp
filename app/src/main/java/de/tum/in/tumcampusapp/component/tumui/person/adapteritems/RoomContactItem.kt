package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.transaction
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchFragment

class RoomContactItem(
    text: String,
    private val roomNumber: String
) : AbstractContactItem(
    R.string.room, text,
    R.drawable.ic_outline_business_24px
) {

    override fun onClick(context: Context) {
        val activity = context as FragmentActivity
        activity.supportFragmentManager.transaction {
            replace(R.id.contentFrame, SearchFragment.newInstance(roomNumber))
        }
    }

}