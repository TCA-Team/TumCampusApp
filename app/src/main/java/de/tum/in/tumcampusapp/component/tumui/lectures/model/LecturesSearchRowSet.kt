package de.tum.`in`.tumcampusapp.component.tumui.lectures.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

/**
 * This class is dealing with the deserialization of the output of TUMOnline to
 * the method "sucheLehrveranstaltungen" or "eigeneLehrveranstaltungen".
 *
 * @see LecturesSearchRow
 */
@Xml(name = "rowset")
data class LecturesSearchRowSet(
        @Element var lehrveranstaltungen: List<LecturesSearchRow> = mutableListOf()  // TODO: Rename variable
)
