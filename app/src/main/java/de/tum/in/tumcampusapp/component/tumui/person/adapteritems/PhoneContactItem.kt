package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class PhoneContactItem(phoneNumber: String) : AbstractContactItem(
    R.string.phone,
    phoneNumber,
    R.drawable.ic_outline_phone_24px
) {

    override fun onClick(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$value"))
        context.startActivity(intent)
    }

}