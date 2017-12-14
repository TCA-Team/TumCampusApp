package de.tum.`in`.tumcampusapp.viewmodel

import android.arch.lifecycle.ViewModel
import android.location.Location
import de.tum.`in`.tumcampusapp.auxiliary.Utils
import de.tum.`in`.tumcampusapp.models.cafeteria.Cafeteria
import de.tum.`in`.tumcampusapp.models.cafeteria.CafeteriaMenu
import de.tum.`in`.tumcampusapp.repository.CafeteriaLocalRepository
import de.tum.`in`.tumcampusapp.repository.CafeteriaRemoteRepository
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for cafeterias.
 */
class CafeteriaViewModel(private val localRepository: CafeteriaLocalRepository,
                         private val remoteRepository: CafeteriaRemoteRepository,
                         private val compositeDisposable: CompositeDisposable) : ViewModel() {

    /**
     * Returns a flowable that emits a list of cafeterias from the local repository.
     */
    fun getAllCafeterias(location: Location): Flowable<List<Cafeteria>> =
            localRepository.getAllCafeterias()
                    .map { transformCafeteria(it, location) }
                    .defaultIfEmpty(emptyList())

    fun getCafeteriaNameFromId(id: Int): Flowable<String> =
            localRepository.getCafeteria(id)
                    .map { it.name }

    fun getCafeteriaMenu(id: Int, date: String): Flowable<List<CafeteriaMenu>> =
            localRepository.getCafeteriaMenu(id,date)
                    .defaultIfEmpty(emptyList())

    fun getAllMenuDates():Flowable<List<String>> =
            localRepository.getAllMenuDates()
                    .defaultIfEmpty(emptyList())

    /**
     * Downloads cafeterias and stores them in the local repository.
     *
     * First checks whether a sync is necessary
     * Then clears current cache
     * Insert new cafeterias
     * Lastly updates last sync
     *
     */
    fun getCafeteriasFromService(force: Boolean): Boolean =
            compositeDisposable.add(Observable.just(1)
                    .filter { localRepository.getLastSync() == null || force }
                    .subscribeOn(Schedulers.computation())
                    .doOnNext { localRepository.clear() }
                    .flatMap { remoteRepository.getAllCafeterias() }.observeOn(Schedulers.io())
                    .doAfterNext { localRepository.updateLastSync() }
                    .doOnError { Utils.logwithTag("CafeteriaViewModel", it.message) }
                    .subscribe({ t -> t.forEach { localRepository.addCafeteria(it) } })
            )

    /**
     * Adds the distance between user and cafeteria to model.
     */
    private fun transformCafeteria(cafeterias: List<Cafeteria>, location: Location): List<Cafeteria> =
            cafeterias.map {
                val results = FloatArray(1)
                Location.distanceBetween(it.latitude, it.longitude, location.latitude, location.longitude, results)
                it.distance = results[0]
                it
            }
}
