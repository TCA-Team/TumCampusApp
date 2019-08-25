package de.tum.`in`.tumcampusapp.component.ui.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.BaseFragment
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.person.PersonDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.roomfinder.RoomFinderDetailsActivity
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Lecture
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Person
import de.tum.`in`.tumcampusapp.component.ui.search.SearchResult.Room
import de.tum.`in`.tumcampusapp.di.ViewModelFactory
import de.tum.`in`.tumcampusapp.di.injector
import de.tum.`in`.tumcampusapp.utils.observeNonNull
import kotlinx.android.synthetic.main.fragment_search.progressIndicator
import kotlinx.android.synthetic.main.fragment_search.searchResultsRecyclerView
import kotlinx.android.synthetic.main.toolbar_search.clearButton
import kotlinx.android.synthetic.main.toolbar_search.searchEditText
import kotlinx.android.synthetic.main.toolbar_search.toolbar
import javax.inject.Inject
import javax.inject.Provider

class SearchFragment : BaseFragment<Unit>(
    R.layout.fragment_search,
    R.string.search
) {

    @Inject
    lateinit var viewModelProvider: Provider<SearchViewModel>

    private val adapter = SearchResultsAdapter(onItemClick = this::onItemClick)

    private val viewModel: SearchViewModel by lazy {
        val factory = ViewModelFactory(viewModelProvider)
        ViewModelProviders.of(this, factory).get(SearchViewModel::class.java)
    }

    private val query: String? by lazy {
        arguments?.getString(KEY_QUERY)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        injector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val backIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)
        backIcon?.let {
            val color = ContextCompat.getColor(requireContext(), R.color.tum_500)
            it.setTint(color)
        }
        toolbar.navigationIcon = backIcon

        toolbar.setNavigationOnClickListener {
            toggleKeyboard(show = false)
            requireActivity().onBackPressed()
        }

        searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = textView.text.toString().trim()
                viewModel.search(input)
                toggleKeyboard(show = false)
                true
            } else {
                false
            }
        }
        searchEditText.requestFocus()
        toggleKeyboard(show = true)

        clearButton.setOnClickListener {
            searchEditText.text.clear()
            viewModel.clear()
            toggleKeyboard(show = true)
        }

        searchResultsRecyclerView.adapter = adapter
        viewModel.viewState.observeNonNull(viewLifecycleOwner, this::render)

        query?.let {
            viewModel.search(it)
        }
    }

    private fun render(viewState: SearchViewState) {
        if (viewState.isLoading) {
            progressIndicator.show()
        } else {
            progressIndicator.hide()
        }

        searchResultsRecyclerView.isVisible = viewState.isLoading.not()
        adapter.submit(viewState.data)
    }

    private fun onItemClick(searchResult: SearchResult) {
        val intent = when (searchResult) {
            is Lecture -> LecturesDetailsActivity.newIntent(requireContext(), searchResult.lecture)
            is Person -> PersonDetailsActivity.newIntent(requireContext(), searchResult.person)
            is Room -> RoomFinderDetailsActivity.newIntent(requireContext(), searchResult.room)
        }
        startActivity(intent)
    }

    private fun toggleKeyboard(show: Boolean) {
        val inputMethodManager = requireContext().getSystemService<InputMethodManager>()

        if (show) {
            searchEditText.requestFocus()
            inputMethodManager?.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        } else {
            searchEditText.clearFocus()
            inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }
    }

    companion object {

        private const val KEY_QUERY = "KEY_QUERY"

        fun newInstance(
            query: String
        ) = SearchFragment().apply {
            arguments = bundleOf(KEY_QUERY to query)
        }

    }

}
