package de.tum.`in`.tumcampusapp.component.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Lecture
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Person
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Room
import kotlinx.android.synthetic.main.list_item_search_more.view.showMoreTextView
import kotlinx.android.synthetic.main.list_item_search_result.view.iconImageView
import kotlinx.android.synthetic.main.list_item_search_result.view.profilePictureImageView
import kotlinx.android.synthetic.main.list_item_search_result.view.subtitleTextView
import kotlinx.android.synthetic.main.list_item_search_result.view.titleTextView

private const val ITEMS_PER_TYPE = 3

enum class Type(@StringRes val textResId: Int) {
    Lecture(R.string.my_lectures),
    Person(R.string.people),
    Room(R.string.rooms)
}

sealed class AdapterItem(@LayoutRes val layoutResId: Int) {

    abstract fun bind(
        holder: SearchResultsAdapter.ViewHolder,
        onItemClick: (AdapterItem) -> Unit
    )

    data class Result(
        val searchResult: SearchResult
    ) : AdapterItem(R.layout.list_item_search_result) {

        override fun bind(
            holder: SearchResultsAdapter.ViewHolder,
            onItemClick: (AdapterItem) -> Unit
        ) = with(holder.itemView) {
            titleTextView.text = searchResult.title
            subtitleTextView.isVisible = searchResult.subtitle.isNullOrBlank().not()
            subtitleTextView.text = searchResult.subtitle

            val imageResource = when (searchResult) {
                is Lecture -> R.drawable.ic_outline_school_24px
                is Person -> R.drawable.ic_person
                is Room -> R.drawable.ic_room
            }
            iconImageView.setImageResource(imageResource)

            if (searchResult is Person) {
                Picasso
                    .get()
                    .load(searchResult.person.fullImageUrl)
                    .into(profilePictureImageView)
            } else {
                // Reset image resource, because otherwise profile pictures might be displayed
                // in the wrong list cells
                profilePictureImageView.setImageDrawable(null)
            }

            setOnClickListener { onItemClick(this@Result) }
        }

    }

    data class More(
        val type: Type,
        val count: Int
    ) : AdapterItem(R.layout.list_item_search_more) {

        override fun bind(
            holder: SearchResultsAdapter.ViewHolder,
            onItemClick: (AdapterItem) -> Unit
        ) = with(holder.itemView) {
            val typeText = context.getString(type.textResId)
            showMoreTextView.text = context.getString(R.string.show_more_format_string, count, typeText)
            setOnClickListener { onItemClick(this@More) }
        }

    }

}

class SearchResultsAdapter(
    private val onItemClick: (SearchResult) -> Unit
) : ListAdapter<AdapterItem, SearchResultsAdapter.ViewHolder>(AdapterItemDiffUtil) {

    private val expandedTypes = mutableListOf<Type>()
    private var items = listOf<SearchResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).bind(holder, this::onItemClick)
    }

    private fun onItemClick(adapterItem: AdapterItem) {
        when (adapterItem) {
            is AdapterItem.Result -> onItemClick(adapterItem.searchResult)
            is AdapterItem.More -> expandSection(adapterItem.type)
        }
    }

    override fun getItemViewType(position: Int) = getItem(position).layoutResId

    fun submit(
        items: List<SearchResult> = this.items,
        expandedTypes: List<Type> = this.expandedTypes
    ) {
        val lectures = items.filterIsInstance(Lecture::class.java)
        val people = items.filterIsInstance(Person::class.java)
        val rooms = items.filterIsInstance(Room::class.java)

        val lecturesCount = if (Type.Lecture in expandedTypes) lectures.size else ITEMS_PER_TYPE
        val peopleCount = if (Type.Person in expandedTypes) people.size else ITEMS_PER_TYPE
        val roomsCount = if (Type.Room in expandedTypes) rooms.size else ITEMS_PER_TYPE

        val lectureItems = lectures.take(lecturesCount).map { AdapterItem.Result(it) }
        val peopleItems = people.take(peopleCount).map { AdapterItem.Result(it) }
        val roomItems = rooms.take(roomsCount).map { AdapterItem.Result(it) }

        val moreLecturesItem = AdapterItem.More(Type.Lecture, lectures.drop(lecturesCount).size)
        val morePeopleItem = AdapterItem.More(Type.Person, people.drop(peopleCount).size)
        val moreRoomsItem = AdapterItem.More(Type.Room, rooms.drop(roomsCount).size)

        val adapterItems = mutableListOf<AdapterItem>()

        if (lectureItems.isNotEmpty()) adapterItems += lectureItems
        if (moreLecturesItem.count != 0) adapterItems += moreLecturesItem

        if (peopleItems.isNotEmpty()) adapterItems += peopleItems
        if (morePeopleItem.count != 0) adapterItems += morePeopleItem

        if (roomItems.isNotEmpty()) adapterItems += roomItems
        if (moreRoomsItem.count != 0) adapterItems += moreRoomsItem

        this.items = items
        submitList(adapterItems)
    }

    private fun expandSection(type: Type) {
        expandedTypes += type
        submit()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}
