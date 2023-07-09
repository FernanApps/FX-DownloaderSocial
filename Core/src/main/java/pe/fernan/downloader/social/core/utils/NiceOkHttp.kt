package pe.fernan.downloader.social.core.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit


const val USER_AGENT = "User-Agent"
const val USER_AGENT_VALUE =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36"

const val ACCEPT = "Accept"

const val ACCEPT_LANGUAGE = "Accept-Language"
const val ACCEPT_LANGUAGE_VALUE = "es-ES,es;q=0.8,en-US;q=0.5,en;q=0.3"

const val CONTENT_TYPE = "Content-Type"
const val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8"

const val CONTENT_LENGTH = "Content-Length"
const val REFERER = "Referer"
const val AUTHORITY = "authority"
const val LOCATION = "Location"


// ::::: NiceOkhttp

class NiceOkHttp(val url: String) {
    var client: OkHttpClient? = null

    fun textHtml(
        vararg headers: Pair<String?, String?> = emptyArray(),
        data: String = "",
        method: Method = Method.GET,
        redirects: Boolean = true,
        userAgent: String = USER_AGENT_VALUE,
        isBigBody: Boolean = false,
        timeOut: Pair<Long, TimeUnit> = 0L to TimeUnit.SECONDS,
        requestBlock: (okhttp3.Request.Builder.() -> Unit)? = null,
    ): String? {
        return baseResponse(
            headers = headers,
            data = data,
            method = method,
            redirects = redirects,
            userAgent = userAgent,
            isBigBody = isBigBody,
            timeOut = timeOut,
            requestBlock = requestBlock
        )?.body?.string()
    }


    fun baseResponse(
        vararg headers: Pair<String?, String?> = emptyArray(),
        data: String = "",
        method: Method = Method.GET,
        redirects: Boolean = true,
        userAgent: String = USER_AGENT_VALUE,
        isBigBody: Boolean = false,
        timeOut: Pair<Long, TimeUnit> = 0L to TimeUnit.SECONDS,
        requestBlock: (okhttp3.Request.Builder.() -> Unit)? = null,
    ): Response? {
        if (client == null) {

            client = OkHttpClient.Builder().followRedirects(redirects).apply {
                val (_timeOut, timeUnit) = timeOut
                if (_timeOut != 0L) {
                    connectTimeout(_timeOut, timeUnit)
                    writeTimeout(_timeOut, timeUnit)
                    readTimeout(_timeOut, timeUnit)
                }

            }.build()
        }
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader(USER_AGENT, userAgent)
            .apply {

                headers.forEach { (key, value) ->
                    if (!key.isNullOrEmpty() && !value.isNullOrEmpty()) {
                        addHeader(key, value)
                    }
                }
                var body: RequestBody? = null
                if (data.isNotEmpty()) {
                    addHeader(CONTENT_TYPE, CONTENT_TYPE_FORM)
                    body = data.toRequestBody(CONTENT_TYPE_FORM.toMediaType())

                }

                when (method) {
                    Method.GET -> get()
                    else -> method(method.toString(), body)
                }


                requestBlock?.invoke(this)
            }
            .build()
        return client?.newCall(request)?.execute()
        //return response
    }


    enum class Method(private val hasBody: Boolean) {
        GET(false),
        POST(true),
        PUT(true),
        DELETE(false),
        PATCH(true),
        HEAD(false);

        fun hasBody(): Boolean {
            return hasBody
        }
    }

    companion object {
        fun connect(url: String): NiceOkHttp {
            return NiceOkHttp(url)
        }

        fun get(
            url: String, requestBlock: (okhttp3.Request.Builder.() -> Unit)? = null,
        ): Response? {
            return NiceOkHttp(url).baseResponse {
                requestBlock?.invoke(this)
            }
        }

    }

}

val okhttp3.Response?.text: String? get() = this?.body?.string()
val okhttp3.Response?.document: org.jsoup.nodes.Document? get() = this?.text?.let { Jsoup.parse(it) }
