package de.tum.`in`.tumcampusapp.component.tumui.lectures.fragment

import android.os.Bundle
import android.view.View
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.NoResultsAdapter
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.lectures.activity.LecturesDetailsActivity
import de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter.LecturesListAdapter
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LecturesResponse
import kotlinx.android.synthetic.main.fragment_lectures.lecturesListView

class LecturesFragment : FragmentForAccessingTumOnline<LecturesResponse>(
    R.layout.fragment_lectures,
    R.string.my_lectures
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lecturesListView.setOnItemClickListener { _, _, position, _ ->
            val item = lecturesListView.getItemAtPosition(position) as Lecture
            val intent = LecturesDetailsActivity.newIntent(requireContext(), item)
            startActivity(intent)
        }

        loadPersonalLectures()
    }

    override fun onRefresh() {
        loadPersonalLectures(CacheControl.BYPASS_CACHE)
    }

    private fun loadPersonalLectures(cacheControl: CacheControl = CacheControl.USE_CACHE) {
        val apiCall = apiClient.getPersonalLectures(cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: LecturesResponse) {
        if (response.lectures.isEmpty()) {
            lecturesListView.adapter = NoResultsAdapter(requireContext())
        } else {
            val lectures = response.lectures.sorted()
            lecturesListView.adapter = LecturesListAdapter(requireContext(), lectures.toMutableList())
        }
    }

    companion object {
        @JvmStatic fun newInstance() = LecturesFragment()
    }

}
