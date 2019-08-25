package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context

abstract class AbstractContactItem(
    val labelResourceId: Int,
    val value: String,
    val iconResourceId: Int
) {

    open fun onClick(context: Context) = Unit

}