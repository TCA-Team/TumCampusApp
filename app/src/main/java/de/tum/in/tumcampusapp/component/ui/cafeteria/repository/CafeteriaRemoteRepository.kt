package de.tum.`in`.tumcampusapp.component.ui.cafeteria.repository

import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import io.reactivex.Observable

class CafeteriaRemoteRepository(private val tumCabeClient: TUMCabeClient) {

    fun getAllCafeterias(): Observable<List<Cafeteria>> = tumCabeClient.cafeterias

}
