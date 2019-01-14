package de.tum.`in`.tumcampusapp.component.ui.cafeteria.details

import android.view.View
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.CafeteriaMenuFormatter
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.FavoriteDishDao
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.FavoriteDish
import kotlinx.android.synthetic.main.card_list_header.view.*
import kotlinx.android.synthetic.main.card_price_line.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

sealed class CafeteriaMenuAdapterItem {

    abstract val id: String

    abstract fun bind(holder: CafeteriaMenusAdapter.ViewHolder)

    data class Header(val menu: CafeteriaMenu) : CafeteriaMenuAdapterItem() {

        override val id: String
            get() = "header_${menu.id}"

        override fun bind(holder: CafeteriaMenusAdapter.ViewHolder) = with(holder.itemView)  {
            headerTextView.text = menu.typeLong.replace("[0-9]", "").trim()
        }

    }

    data class Item(
            val menu: CafeteriaMenu,
            val isFavorite: Boolean = false,
            val rolePrice: String? = null,
            val isBigLayout: Boolean,
            val favoriteDishDao: FavoriteDishDao
    ) : CafeteriaMenuAdapterItem() {

        override val id: String
            get() = "item_${menu.id}"

        override fun bind(holder: CafeteriaMenusAdapter.ViewHolder) = with(holder.itemView) {
            val formatter = CafeteriaMenuFormatter(context)
            val menuName = if (isBigLayout) menu.name else formatter.prepare(menu.name)
            val menuSpan = formatter.menuToSpan(menuName)

            val nameTextView = findViewById<TextView>(R.id.nameTextView)
            nameTextView.text = menuSpan

            rolePrice?.let { showPrice(this, it) } ?: hidePrice(this)
        }

        private fun showPrice(itemView: View, price: String) = with(itemView) {
            priceTextView.text = kotlin.String.format("%s €", price)
            favoriteDish.isSelected = isFavorite
            favoriteDish.setOnClickListener { view ->
                if (!view.isSelected) {
                    val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")
                    val date = formatter.print(DateTime.now())
                    favoriteDishDao.insertFavouriteDish(FavoriteDish.create(menu, date))
                    view.isSelected = true
                } else {
                    favoriteDishDao.deleteFavoriteDish(menu.cafeteriaId, menu.name)
                    view.isSelected = false
                }
            }
        }

        private fun hidePrice(itemView: View) = with(itemView) {
            priceTextView.visibility = View.GONE
            favoriteDish.visibility = View.GONE
        }

    }

}
