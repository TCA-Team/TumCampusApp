package de.tum.`in`.tumcampusapp.component.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.person.model.Person
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.model.RoomFinderRoom
import de.tum.`in`.tumcampusapp.utils.plusAssign
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

typealias ApiPerson = Person
typealias ApiLecture = Lecture

data class SearchViewState(
    val data: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false
)

sealed class SearchResult {
    abstract val title: String
    open val subtitle: String? = null

    data class Lecture(val lecture: ApiLecture) : SearchResult() {
        override val title: String
            get() = lecture.title

        override val subtitle: String?
            get() = lecture.lecturers
    }

    data class Person(val person: ApiPerson) : SearchResult() {
        override val title: String
            get() = person.fullName
    }

    data class Room(val room: RoomFinderRoom) : SearchResult() {
        override val title: String
            get() = room.info

        override val subtitle: String?
            get() = room.formattedAddress
    }
}

sealed class Action {
    object Clear : Action()
    object Search : Action()
    data class ShowResults(val results: List<SearchResult>) : Action()
}

class SearchResultsStore {

    private val _results = MutableLiveData<List<SearchResult>>()
    val results: LiveData<List<SearchResult>> = _results

    private val _viewState = MutableLiveData<SearchViewState>()
    val viewState: LiveData<SearchViewState> = _viewState

    init {
        _viewState.value = SearchViewState()
    }

    fun dispatch(action: Action) {
        _viewState.value = reduce(state(), action)
    }

    private fun reduce(
        state: SearchViewState,
        action: Action
    ) = when (action) {
        is Action.Clear -> state.copy(data = emptyList(), isLoading = false)
        is Action.Search -> state.copy(data = emptyList(), isLoading = true)
        is Action.ShowResults -> state.copy(data = state.data + action.results, isLoading = false)
    }

    private fun state() = checkNotNull(_viewState.value)

}

class SearchViewModel @Inject constructor(
    private val tumOnlineClient: TUMOnlineClient,
    private val tumCabeClient: TUMCabeClient
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val store = SearchResultsStore()

    val viewState: LiveData<SearchViewState> = store.viewState

    fun search(query: String) {
        store.dispatch(Action.Search)

        val persons = tumOnlineClient
            .searchPerson(query)
            .subscribeOn(Schedulers.io())
            .map { it.persons }
            .onErrorReturn { emptyList() }
            .map { persons -> persons.map { SearchResult.Person(it) } }

        val rooms = tumCabeClient
            .fetchRooms(userRoomSearchMatching(query))
            .subscribeOn(Schedulers.io())
            .onErrorReturn { emptyList() }
            .map { rooms -> rooms.map { SearchResult.Room(it) } }

        val lectures = tumOnlineClient
            .searchLecturesRx(query)
            .subscribeOn(Schedulers.io())
            .map { it.lectures }
            .onErrorReturn { emptyList() }
            .map { lectures -> lectures.map { SearchResult.Lecture(it) } }

        compositeDisposable += Single
            .merge(persons, rooms, lectures)
            .observeOn(AndroidSchedulers.mainThread())
            .map { Action.ShowResults(it) }
            .subscribe { action -> store.dispatch(action) }
    }

    fun clear() {
        store.dispatch(Action.Clear)
    }

    /**
     * Distinguishes between some room searches, eg. MW 2001 or MI 01.15.069 and takes the
     * number part so that the search can return (somewhat) meaningful results
     * (Temporary and non-optimal)
     *
     * @return a new query or the original one if nothing was matched
     */
    // TODO
    private fun userRoomSearchMatching(roomSearchQuery: String): String {
        // Matches the number part if the String is composed of two words, probably wrong:

        // First group captures numbers with dots, like the 01.15.069 part from 'MI 01.15.069'
        // (This is the best search format for MI room numbers)
        // The second group captures numbers and mixed formats with letters, like 'MW2001'
        // Only the first match will be returned
        val pattern = Pattern.compile("(\\w+(?:\\.\\w+)+)|(\\w+\\d+)")

        val matcher = pattern.matcher(roomSearchQuery)

        return if (matcher.find()) {
            matcher.group()
        } else {
            roomSearchQuery
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

}
