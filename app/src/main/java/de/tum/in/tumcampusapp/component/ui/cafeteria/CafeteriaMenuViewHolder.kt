package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.CafeteriaMenusAdapter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.details.OpenHoursHelper
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaWithMenus
import de.tum.`in`.tumcampusapp.component.ui.overview.card.CardViewHolder
import kotlinx.android.synthetic.main.card_cafeteria_menu.view.*
import org.joda.time.format.DateTimeFormat

class CafeteriaMenuViewHolder(itemView: View) : CardViewHolder(itemView) {

    private var isFirstBind = true
    private lateinit var adapter: CafeteriaMenusAdapter

    fun bind(cafeteria: CafeteriaWithMenus) = with(itemView) {
        cafeteriaNameTextView.text = cafeteria.name
        menuDateTextView.text = DateTimeFormat.mediumDate().print(cafeteria.nextMenuDate)

        val openHoursHelper = OpenHoursHelper(context)
        val openingHours = openHoursHelper.getHoursByIdAsString(cafeteria.id, cafeteria.nextMenuDate)
        openingHoursTextView.text = openingHours

        if (isFirstBind) {
            menusRecyclerView.layoutManager = LinearLayoutManager(context)
            menusRecyclerView.itemAnimator = DefaultItemAnimator()

            adapter = CafeteriaMenusAdapter(context, false)
            menusRecyclerView.adapter = adapter

            isFirstBind = false
        }

        adapter.update(cafeteria.menus)
    }

}
