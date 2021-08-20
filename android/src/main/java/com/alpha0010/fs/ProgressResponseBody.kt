package com.alpha0010.fs

import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer

typealias ProgressListener = (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit

const val MIN_EVENT_INTERVAL = 150L

class ProgressResponseBody(
  private val responseBody: ResponseBody,
  private val listener: ProgressListener
) : ResponseBody() {
  private var bufferedSource: BufferedSource? = null
  private var lastEventTime = 0L

  override fun contentType() = responseBody.contentType()

  override fun contentLength() = responseBody.contentLength()

  override fun source(): BufferedSource {
    return bufferedSource ?: object : ForwardingSource(responseBody.source()) {
      var totalBytesRead = 0L

      override fun read(sink: Buffer, byteCount: Long): Long {
        val bytesRead = super.read(sink, byteCount)
        val isDone = bytesRead == -1L
        totalBytesRead += if (isDone) 0 else bytesRead

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime > MIN_EVENT_INTERVAL || isDone) {
          lastEventTime = currentTime
          listener(totalBytesRead, contentLength(), isDone)
        }

        return bytesRead
      }
    }.buffer().also { bufferedSource = it }
  }
}
