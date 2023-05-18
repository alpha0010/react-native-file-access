package com.alpha0010.fs

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.modules.network.OkHttpClientProvider
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.net.SocketFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val FETCH_EVENT = "FetchEvent"

class NetworkHandler(private val context: ReactContext) {
  private val emitter = context.getJSModule(RCTDeviceEventEmitter::class.java)

  suspend fun fetch(
    requestId: Int,
    resource: String,
    init: ReadableMap,
    onComplete: () -> Unit
  ): Call? {
    val request = try {
      buildRequest(resource, init)
    } catch (e: Throwable) {
      onComplete()
      onFetchError(requestId, e)
      return null
    }

    val call = try {
      val unmetered = init.hasKey("network")
        && init.getString("network") == "unmetered"

      // Share client with RN core library.
      getClient(unmetered) { bytesRead, contentLength, done ->
        emitter.emit(
          FETCH_EVENT, Arguments.makeNativeMap(
            mapOf(
              "requestId" to requestId,
              "state" to "progress",
              "bytesRead" to bytesRead,
              "contentLength" to contentLength,
              "done" to done
            )
          )
        )
      }
    } catch (e: Throwable) {
      onComplete()
      onFetchError(requestId, e)
      return null
    }.newCall(request)

    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        onComplete()
        onFetchError(requestId, e)
      }

      override fun onResponse(call: Call, response: Response) {
        try {
          response.use {
            if (init.hasKey("path")) {
              parsePathToFile(init.getString("path")!!)
                .outputStream()
                .use { response.body!!.byteStream().copyTo(it) }
            }

            onComplete()
            val headers = response.headers.names().map { it to response.header(it) }
            emitter.emit(
              FETCH_EVENT, Arguments.makeNativeMap(
                mapOf(
                  "requestId" to requestId,
                  "state" to "complete",
                  "headers" to Arguments.makeNativeMap(headers.toMap()),
                  "ok" to response.isSuccessful,
                  "redirected" to response.isRedirect,
                  "status" to response.code,
                  "statusText" to response.message,
                  "url" to response.request.url.toString()
                )
              )
            )
          }
        } catch (e: Throwable) {
          onComplete()
          onFetchError(requestId, e)
        }
      }
    })

    return call
  }

  private fun buildRequest(resource: String, init: ReadableMap): Request {
    // Request will be saved to a file, no reason to also save in cache.
    val builder = Request.Builder()
      .url(resource)
      .cacheControl(CacheControl.Builder().noStore().build())

    if (init.hasKey("method")) {
      if (init.hasKey("body")) {
        builder.method(
          init.getString("method")!!,
          init.getString("body")!!.toRequestBody(null)
        )
      } else {
        builder.method(init.getString("method")!!, null)
      }
    }

    if (init.hasKey("headers")) {
      for (header in init.getMap("headers")!!.entryIterator) {
        builder.header(header.key, header.value as String)
      }
    }

    return builder.build()
  }

  /**
   * Attempt to get an unmetered network.
   */
  private suspend fun getUnmeteredNetwork(): SocketFactory = suspendCoroutine { continuation ->
    val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val cb = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        mgr.unregisterNetworkCallback(this)
        continuation.resume(network.socketFactory)
      }

      override fun onUnavailable() {
        mgr.unregisterNetworkCallback(this)
        continuation.resumeWithException(Exception("Unmetered network unavailable."))
      }
    }

    mgr.registerNetworkCallback(
      NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        .build(),
      cb
    )
  }

  private suspend fun getClient(unmetered: Boolean, listener: ProgressListener): OkHttpClient {
    return OkHttpClientProvider
      .getOkHttpClient()
      .newBuilder()
      .addNetworkInterceptor { chain ->
        val originalResponse = chain.proceed(chain.request())
        originalResponse.body
          ?.let { originalResponse.newBuilder().body(ProgressResponseBody(it, listener)).build() }
          ?: originalResponse
      }
      .apply {
        if (unmetered) {
          socketFactory(getUnmeteredNetwork())
        }
      }
      .build()
  }

  private fun onFetchError(requestId: Int, e: Throwable) {
    emitter.emit(
      FETCH_EVENT, Arguments.makeNativeMap(
        mapOf(
          "requestId" to requestId,
          "state" to "error",
          "message" to e.localizedMessage
        )
      )
    )
  }
}
