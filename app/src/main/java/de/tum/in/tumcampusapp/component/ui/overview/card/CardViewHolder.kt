package de.tum.`in`.tumcampusapp.component.ui.overview.card

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.navigation.NavigationManager
import de.tum.`in`.tumcampusapp.component.other.settings.UserPreferencesActivity
import de.tum.`in`.tumcampusapp.utils.Const

open class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var currentCard: Card? = null

    private val context: Context by lazy { itemView.context }
    private val activity: Activity by lazy { context as Activity }

    init {
        itemView.setOnClickListener {
            val destination = currentCard?.getNavigationDestination()
            destination?.let { dest ->
                NavigationManager.open(activity, dest)
            }
        }

        val moreIcon = itemView.findViewById<ImageButton>(R.id.cardMoreIcon)
        moreIcon?.setOnClickListener { openOptionsPopup(it) }
    }

    private fun openOptionsPopup(anchorView: View) {
        PopupMenu(context, anchorView, Gravity.BOTTOM).apply {
            menuInflater.inflate(R.menu.card_popup_menu, menu)
            setOnMenuItemClickListener { onOptionSelected(it) }
            show()
        }
    }

    private fun onOptionSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_card_setting -> openCardSettings()
            R.id.always_hide_card -> alwaysHideCard()
        }

        return true
    }

    private fun openCardSettings() {
        val key = currentCard?.settingsPrefix ?: return

        val intent = Intent(context, UserPreferencesActivity::class.java).apply {
            putExtra(Const.PREFERENCE_SCREEN, key)
        }
        context.startActivity(intent)
    }

    private fun alwaysHideCard() {
        currentCard?.hideAlways()
        currentCard?.discardCard()
    }

}