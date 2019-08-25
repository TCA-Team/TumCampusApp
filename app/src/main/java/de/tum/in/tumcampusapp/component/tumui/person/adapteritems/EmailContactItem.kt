package de.tum.`in`.tumcampusapp.component.tumui.person.adapteritems

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.tum.`in`.tumcampusapp.R

class EmailContactItem(emailAddress: String) : AbstractContactItem(
    R.string.e_mail,
    emailAddress,
    R.drawable.ic_outline_email_24px
) {

    override fun onClick(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            type = "text/plain"
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(value))
        }
        context.startActivity(intent)
    }

}