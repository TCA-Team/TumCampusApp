package de.tum.in.tumcampusapp.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Wrapper class holding a list of tuitions ; based on ExamList
 *
 * Note: This model is based on the TUMOnline web service response format for a
 * corresponding request.
 */
@Root(name = "rowset")
public class TuitionList {

	@ElementList(inline = true)
	private List<Tuition> tuitions;

	public List<Tuition> getTuitions() {
		return tuitions;
	}

}
