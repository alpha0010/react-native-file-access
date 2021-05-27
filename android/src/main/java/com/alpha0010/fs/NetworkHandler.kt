package com.alpha0010.fs

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.modules.network.OkHttpClientProvider
import okhttp3.*
import java.io.IOException

const val FETCH_EVENT = "FetchEvent"

class NetworkHandler(reactContext: ReactContext) {
  private val emitter = reactContext.getJSModule(RCTDeviceEventEmitter::class.java)

  fun fetch(requestId: Int, resource: String, init: ReadableMap) {
    val request = try {
      buildRequest(resource, init)
    } catch (e: Throwable) {
      onFetchError(requestId, e)
      return
    }

    // Share client with RN core library.
    val call = getClient { bytesRead, contentLength, done ->
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
    }.newCall(request)
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        onFetchError(requestId, e)
      }

      override fun onResponse(call: Call, response: Response) {
        try {
          response.use {
            if (init.hasKey("path")) {
              parsePathToFile(init.getString("path")!!)
                .outputStream()
                .use { response.body()!!.byteStream().copyTo(it) }
            }

            val headers = response.headers().names().map { it to response.header(it) }
            emitter.emit(
              FETCH_EVENT, Arguments.makeNativeMap(
                mapOf(
                  "requestId" to requestId,
                  "state" to "complete",
                  "headers" to Arguments.makeNativeMap(headers.toMap()),
                  "ok" to response.isSuccessful,
                  "redirected" to response.isRedirect,
                  "status" to response.code(),
                  "statusText" to response.message(),
                  "url" to response.request().url().toString()
                )
              )
            )
          }
        } catch (e: Throwable) {
          onFetchError(requestId, e)
        }
      }
    })
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
          RequestBody.create(null, init.getString("body")!!)
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

  private fun getClient(listener: ProgressListener): OkHttpClient {
    return OkHttpClientProvider
      .getOkHttpClient()
      .newBuilder()
      .addNetworkInterceptor { chain ->
        val originalResponse = chain.proceed(chain.request())
        originalResponse.body()
          ?.let { originalResponse.newBuilder().body(ProgressResponseBody(it, listener)).build() }
          ?: originalResponse
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
