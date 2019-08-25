package de.tum.`in`.tumcampusapp.component.tumui.lectures.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.Lecture
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureDetails
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureDetailsResponse
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.activity_lecturedetails.appointmentsButton
import kotlinx.android.synthetic.main.activity_lecturedetails.contentTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.dateTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.examinationAidsHeaderTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.examinationAidsTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.lectureNameTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.methodHeaderTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.methodTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.orgTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.professorTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.semesterTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.swsTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.targetsHeaderTextView
import kotlinx.android.synthetic.main.activity_lecturedetails.targetsTextView

/**
 * This Activity will show all details found on the TUMOnline web service
 * identified by its lecture id (which has to be posted to this activity by
 * bundle).
 *
 *
 * There is also the opportunity to get all appointments which are related to
 * this lecture by clicking the button on top of the view.
 *
 *
 * HINT: a valid TUM Online token is needed
 *
 *
 * NEEDS: stp_sp_nr set in incoming bundle (lecture id)
 */
class LecturesDetailsActivity : ActivityForAccessingTumOnline<LectureDetailsResponse>(R.layout.activity_lecturedetails) {

    private lateinit var currentItem: LectureDetails
    private lateinit var mLectureId: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appointmentsButton.setOnClickListener {
            // LectureAppointments need the name and id of the facing lecture
            val bundle = Bundle()
            bundle.putString("stp_sp_nr", currentItem.stp_sp_nr)
            bundle.putString(Const.TITLE_EXTRA, currentItem.title)

            val intent = Intent(this, LecturesAppointmentsActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        mLectureId = intent.getStringExtra("stp_sp_nr")
        loadLectureDetails(mLectureId, CacheControl.USE_CACHE)
    }

    override fun onRefresh() {
        loadLectureDetails(mLectureId, CacheControl.BYPASS_CACHE)
    }

    private fun loadLectureDetails(lectureId: String, cacheControl: CacheControl) {
        val apiCall = apiClient.getLectureDetails(lectureId, cacheControl)
        fetch(apiCall)
    }

    override fun onDownloadSuccessful(response: LectureDetailsResponse) {
        val lectureDetails = response.lectureDetails
        if (lectureDetails.isEmpty()) {
            Utils.showToast(this, R.string.error_no_data_to_show)
            finish()
            return
        }
        currentItem = lectureDetails[0]

        lectureNameTextView.text = currentItem.title

        val strLectureLanguage = StringBuilder(currentItem.semesterName
            ?: getString(R.string.unknown))
        if (currentItem.mainLanguage != null) {
            strLectureLanguage.append(" - ").append(currentItem.mainLanguage)
        }
        semesterTextView.text = strLectureLanguage

        swsTextView.text = getString(R.string.lecture_details_format_string, currentItem.lectureType, currentItem.duration)
        professorTextView.text = currentItem.lecturers
        orgTextView.text = currentItem.chairName
        contentTextView.text = currentItem.lectureContent
        dateTextView.text = currentItem.firstAppointment

        val teachingMethod = currentItem.teachingMethod
        if (teachingMethod == null || teachingMethod.isEmpty()) {
            methodHeaderTextView.isVisible = false
            methodTextView.isVisible = false
        } else {
            methodTextView.text = teachingMethod
        }

        val targets = currentItem.teachingTargets
        if (targets == null || targets.isEmpty()) {
            targetsHeaderTextView.isVisible = false
            targetsTextView.isVisible = false
        } else {
            targetsTextView.text = targets
        }

        val aids = currentItem.examinationAids
        if (aids == null || aids.isEmpty()) {
            examinationAidsHeaderTextView.isVisible = false
            examinationAidsTextView.isVisible = false
        } else {
            examinationAidsTextView.text = aids
        }

        appointmentsButton.isEnabled = true
    }

    companion object {

        fun newIntent(
            context: Context,
            lecture: Lecture
        ) = Intent(context, LecturesDetailsActivity::class.java).apply {
            putExtra(Lecture.STP_SP_NR, lecture.stp_sp_nr)
        }

    }

}
