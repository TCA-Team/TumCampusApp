package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class MobilePhoneContactItem(text: String) : AbstractContactItem(
    R.string.mobile_phone,
    text,
    R.drawable.ic_outline_phone_24px
) {

    override fun onClick(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$value"))
        context.startActivity(intent)
    }

}