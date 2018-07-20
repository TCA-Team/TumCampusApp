package de.tum.`in`.tumcampusapp.api.tumonline.interceptors

import android.content.Context
import android.preference.PreferenceManager
import com.tickaroo.tikxml.TikXml
import de.tum.`in`.tumcampusapp.api.tumonline.exception.*
import de.tum.`in`.tumcampusapp.api.tumonline.model.Error
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import de.tum.`in`.tumcampusapp.utils.tryOrNull
import okhttp3.Interceptor
import okhttp3.Response

class TUMOnlineInterceptor(private val context: Context) : Interceptor {

    private val accessToken: String?
        get() = loadAccessTokenFromPreferences(context)

    private val tikXml = TikXml.Builder()
            .exceptionOnUnreadXml(false)
            .build()

    private val cachingHelper = CachingHelper()

    @Throws(RequestLimitReachedException::class,
            TokenLimitReachedException::class,
            InvalidTokenException::class,
            MissingPermissionException::class,
            UnknownErrorException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().toString()

        // Check for special requests
        val path = request.url().encodedPath()
        val isTokenRequest = path.contains("requestToken")
        val isTokenConfirmationCheck = path.contains("isTokenConfirmed")

        // TUMonline requests are disabled if a request previously threw an InvalidTokenException
        val isTumOnlineDisabled = Utils.getSettingBool(context, Const.TUMO_DISABLED, false)

        if (!isTokenRequest && !isTokenConfirmationCheck && isTumOnlineDisabled) {
            //throw InvalidTokenException()
            //val exception = InvalidTokenException()
            //return exception.transformToErrorResponse()
            // TODO
        }

        // Add the access token as a parameter to the URL
        var modifiedUrl = request.url()
        accessToken?.let {
            modifiedUrl = modifiedUrl
                    .newBuilder()
                    .addQueryParameter("pToken", it)
                    .build()
        }

        val modifiedRequest = request.newBuilder().url(modifiedUrl).build()

        // Send the request to TUMonline
        var response = chain.proceed(modifiedRequest)
        val peekBody = response.peekBody(Long.MAX_VALUE)

        // The server always returns 200. To detect errors, we attempt to parse the response into
        // an Error. If this fails, we know that we got a non-error response from TUMonline.
        val error = tryOrNull { tikXml.read(peekBody.source(), Error::class.java) }
        error?.let { e ->
            if (e.exception is InvalidTokenException) {
                // If it is an InvalidTokenException, we disable interaction with TUMonline.
                Utils.setSetting(context, Const.TUMO_DISABLED, true)
            }

            return e.toErrorResponse(response)
            /*
            throw it.exception.also {
                if (it is InvalidTokenException) {
                    // If it is an InvalidTokenException, we disable interaction with TUMonline.
                    Utils.setSetting(context, Const.TUMO_DISABLED, true)
                }
            }
            */
        }

        // We did not receive an error, so we can cache the response.
        val isCacheable = cachingHelper.isCacheable(url)
        if (response.isSuccessful && isCacheable) {
            response = cachingHelper.updateCacheControlHeader(url, response)
        }

        // Because the request did not return an Error, we can re-enable TUMonline request
        // if they have been disabled before
        Utils.setSetting(context, Const.TUMO_DISABLED, false)

        return response
    }

    private fun loadAccessTokenFromPreferences(context: Context): String? {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(Const.ACCESS_TOKEN, null)
    }

}