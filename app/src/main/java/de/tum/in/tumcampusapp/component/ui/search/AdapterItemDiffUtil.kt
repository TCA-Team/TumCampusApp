package de.tum.`in`.tumcampusapp.component.ui.search

import androidx.recyclerview.widget.DiffUtil
import de.tum.`in`.tumcampusapp.component.ui.search.AdapterItem.More
import de.tum.`in`.tumcampusapp.component.ui.search.AdapterItem.Result
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Lecture
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Person
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Room

internal object AdapterItemDiffUtil : DiffUtil.ItemCallback<AdapterItem>() {

    override fun areItemsTheSame(
        oldItem: AdapterItem,
        newItem: AdapterItem
    ) = when {
        oldItem is Result && newItem is Result -> {
            areSearchResultsTheSame(oldItem.searchResult, newItem.searchResult)
        }
        oldItem is More && newItem is More -> oldItem.type == newItem.type
        else -> false
    }

    override fun areContentsTheSame(
        oldItem: AdapterItem,
        newItem: AdapterItem
    ) = when {
        oldItem is Result && newItem is Result -> {
            areSearchResultContentsTheSame(oldItem.searchResult, newItem.searchResult)
        }
        oldItem is More && newItem is More -> {
            oldItem.type == newItem.type && oldItem.count == newItem.count
        }
        else -> false
    }

    private fun areSearchResultsTheSame(
        oldItem: SearchResult,
        newItem: SearchResult
    ) = when {
        oldItem is Lecture && newItem is Lecture -> {
            oldItem.lecture.lectureId == newItem.lecture.lectureId
        }
        oldItem is Person && newItem is Person -> oldItem.person.id == newItem.person.id
        oldItem is Room && newItem is Room -> oldItem.room.room_id == newItem.room.room_id
        else -> false
    }

    private fun areSearchResultContentsTheSame(
        oldItem: SearchResult,
        newItem: SearchResult
    ) = when {
        oldItem is Lecture && newItem is Lecture -> oldItem.lecture == newItem.lecture
        oldItem is Person && newItem is Person -> oldItem.person == newItem.person
        oldItem is Room && newItem is Room -> oldItem.room == newItem.room
        else -> false
    }

}
