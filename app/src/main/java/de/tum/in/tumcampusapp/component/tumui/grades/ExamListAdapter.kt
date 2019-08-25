package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.adapter.SimpleStickyListHeadersAdapter
import de.tum.`in`.tumcampusapp.component.tumui.grades.model.Exam
import org.joda.time.format.DateTimeFormat

/**
 * Custom UI adapter for a list of exams.
 */
class ExamListAdapter(
    context: Context,
    results: List<Exam>
) : SimpleStickyListHeadersAdapter<Exam>(context, results.toMutableList()) {

    init {
        itemList.sort()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.activity_grades_listview, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val exam = itemList[position]
        holder.nameTextView.text = exam.course
        holder.gradeTextView.text = exam.grade

        val gradeColor = exam.getGradeColor(context)
        holder.gradeTextView.background.setTint(gradeColor)

        val date: String = if (exam.date == null) {
            context.getString(R.string.not_specified)
        } else {
            DATE_FORMAT.print(exam.date)
        }
        holder.examDateTextView.text = String.format("%s: %s", context.getString(R.string.date), date)

        holder.additionalInfoTextView.text = String.format("%s: %s, %s: %s",
                context.getString(R.string.examiner), exam.examiner,
                context.getString(R.string.mode), exam.modus)

        return view
    }

    override fun generateHeaderName(item: Exam): String {
        val headerText = super.generateHeaderName(item)
        val year = Integer.parseInt(headerText.substring(0, 2))
        return if (headerText[2] == 'W') {
            context.getString(R.string.winter_semester, year, year + 1)
        } else {
            context.getString(R.string.summer_semester, year)
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class ViewHolder(itemView: View) {
        var nameTextView: TextView = itemView.findViewById(R.id.courseNameTextView)
        var gradeTextView: TextView = itemView.findViewById(R.id.gradeTextView)
        var examDateTextView: TextView = itemView.findViewById(R.id.examDateTextView)
        var additionalInfoTextView: TextView = itemView.findViewById(R.id.additionalInfoTextView)
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormat.mediumDate()
    }
}
