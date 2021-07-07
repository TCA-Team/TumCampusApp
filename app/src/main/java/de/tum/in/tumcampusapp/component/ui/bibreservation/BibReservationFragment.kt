package de.tum.`in`.tumcampusapp.component.ui.bibreservation

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.other.generic.fragment.FragmentForAccessingTumCabe
import de.tum.`in`.tumcampusapp.component.ui.bibreservation.model.Bib
import de.tum.`in`.tumcampusapp.component.ui.bibreservation.model.BibAppointment
import kotlinx.android.synthetic.main.fragment_bib_reservation.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BibReservationFragment : FragmentForAccessingTumCabe<List<BibAppointment>>(R.layout.fragment_bib_reservation, R.string.bib_reservation),
    Callback<List<BibAppointment>>, AdapterView.OnItemSelectedListener {

    private var allAppointments: ArrayList<BibAppointment> = ArrayList()
    private val appointments: ArrayList<BibAppointment> = ArrayList()
    private lateinit var reservationAdapter: BibReservationAdapter
    var selectedBib: Bib = Bib.Stammgelaende
    private val bibs = arrayListOf(Bib.Stammgelaende, Bib.MI, Bib.Chemie, Bib.Maschinenwesen, Bib.Medizin, Bib.Physik, Bib.Sport, Bib.Straubing, Bib.Weihenstephan)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reservationAdapter = BibReservationAdapter(appointments, requireContext())
        enableRefresh()
        bibAppointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bibAppointmentsRecyclerView.adapter = reservationAdapter
        bibSelectionSpinner.onItemSelectedListener = this

        swipeRefreshLayout?.isRefreshing = true
        apiClient.getReservableBibAppointments(this)
    }

    private fun updateList() {
        appointments.clear()
        appointments.addAll(allAppointments.filter { it.bib == selectedBib.toString() } as Collection<BibAppointment>)
        reservationAdapter.notifyDataSetChanged()
    }

    override fun onRefresh() {
        apiClient.getReservableBibAppointments(this)
    }

    override fun onResponse(call: Call<List<BibAppointment>>, response: Response<List<BibAppointment>>) {
        allAppointments = response.body() as ArrayList<BibAppointment>
        updateList()
        swipeRefreshLayout?.isRefreshing = false
    }

    override fun onFailure(call: Call<List<BibAppointment>>, t: Throwable) {
        showErrorLayout()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedBib = bibs[position]
        updateList()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectedBib = Bib.Unknown
    }
}
