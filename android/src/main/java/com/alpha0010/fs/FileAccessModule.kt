package com.alpha0010.fs

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Base64
import com.facebook.react.bridge.*
import com.facebook.react.modules.network.OkHttpClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.Callback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest

class FileAccessModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun getName(): String {
    return "RNFileAccess"
  }

  override fun getConstants(): MutableMap<String, String?> {
    val sdCardDir = try {
      // Search via env may not be reliable. Recent Android versions
      // discourage/restrict full access to public locations.
      System.getenv("SECONDARY_STORAGE") ?: System.getenv("EXTERNAL_STORAGE")
    } catch (e: Throwable) {
      null
    }

    return hashMapOf(
      "CacheDir" to reactApplicationContext.cacheDir.absolutePath,
      "DatabaseDir" to reactApplicationContext.getDatabasePath("FileAccessProbe").parent,
      "DocumentDir" to reactApplicationContext.filesDir.absolutePath,
      "MainBundleDir" to reactApplicationContext.applicationInfo.dataDir,
      "SDCardDir" to sdCardDir,
    )
  }

  @ReactMethod
  fun appendFile(path: String, data: String, encoding: String, promise: Promise) {
    ioScope.launch {
      try {
        if (encoding == "base64") {
          parsePathToFile(path).appendBytes(Base64.decode(data, Base64.DEFAULT))
        } else {
          parsePathToFile(path).appendText(data)
        }
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun concatFiles(source: String, target: String, promise: Promise) {
    try {
      openForReading(source).use { input ->
        FileOutputStream(parsePathToFile(target), true).use {
          promise.resolve(input.copyTo(it).toInt())
        }
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun cp(source: String, target: String, promise: Promise) {
    ioScope.launch {
      try {
        openForReading(source).use { input ->
          parsePathToFile(target).outputStream().use { input.copyTo(it) }
        }
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun cpAsset(asset: String, target: String, type: String, promise: Promise) {
    try {
      if (type == "resource") {
        val id = reactApplicationContext.resources.getIdentifier(
          asset,
          null,
          reactApplicationContext.packageName
        )
        reactApplicationContext.resources.openRawResource(id)
      } else {
        reactApplicationContext.assets.open(asset)
      }.use { assetStream ->
        parsePathToFile(target).outputStream().use { assetStream.copyTo(it) }
      }
      promise.resolve(null)
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun cpExternal(source: String, targetName: String, dir: String, promise: Promise) {
    try {
      openForReading(source).use { input ->
        if (dir == "downloads") {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            reactApplicationContext.contentResolver.insert(
              MediaStore.Downloads.EXTERNAL_CONTENT_URI,
              ContentValues().apply { put(MediaStore.Downloads.DISPLAY_NAME, targetName) }
            )?.let { reactApplicationContext.contentResolver.openOutputStream(it) }
          } else {
            @Suppress("DEPRECATION")
            File(
              Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
              targetName
            ).outputStream()
          }
        } else {
          when (dir) {
            "audio" -> {
              reactApplicationContext.contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply { put(MediaStore.Audio.Media.DISPLAY_NAME, targetName) }
              )
            }
            "images" -> {
              reactApplicationContext.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME, targetName) }
              )
            }
            "video" -> {
              reactApplicationContext.contentResolver.insert(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                ContentValues().apply { put(MediaStore.Video.Media.DISPLAY_NAME, targetName) }
              )
            }
            else -> null
          }?.let { reactApplicationContext.contentResolver.openOutputStream(it) }
        }?.use { output ->
          try {
            input.copyTo(output)
            promise.resolve(null)
          } catch (e: Throwable) {
            promise.reject(e)
          }
          return
        }

        promise.reject("ERR", "Failed to copy to '$targetName' ('$dir')")
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun df(promise: Promise) {
    ioScope.launch {
      try {
        val internalStat = StatFs(reactApplicationContext.filesDir.absolutePath)
        val results = mutableMapOf(
          "internal_free" to internalStat.availableBytes,
          "internal_total" to internalStat.totalBytes
        )

        val externalDir = reactApplicationContext.getExternalFilesDir(null)
        if (externalDir != null) {
          val externalStat = StatFs(externalDir.absolutePath)
          results["external_free"] = externalStat.availableBytes
          results["external_total"] = externalStat.totalBytes
        }

        promise.resolve(Arguments.makeNativeMap(results as Map<String, Any>?))
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun exists(path: String, promise: Promise) {
    ioScope.launch {
      try {
        promise.resolve(parsePathToFile(path).exists())
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun fetch(resource: String, init: ReadableMap, promise: Promise) {
    val request = try {
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

      builder.build()
    } catch (e: Throwable) {
      promise.reject(e)
      return
    }

    // Share client with RN core library.
    val call = OkHttpClientProvider.getOkHttpClient().newCall(request)
    call.enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        promise.reject(e)
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
            promise.resolve(Arguments.makeNativeMap(mapOf(
              "headers" to Arguments.makeNativeMap(headers.toMap()),
              "ok" to response.isSuccessful,
              "redirected" to response.isRedirect,
              "status" to response.code(),
              "statusText" to response.message(),
              "url" to response.request().url().toString()
            )))
          }
        } catch (e: Throwable) {
          promise.reject(e)
        }
      }
    })
  }

  @ReactMethod
  fun hash(path: String, algorithm: String, promise: Promise) {
    try {
      val digest = MessageDigest.getInstance(algorithm)
      openForReading(path).use {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = it.read(buffer)
        while (bytes >= 0) {
          digest.update(buffer, 0, bytes)
          bytes = it.read(buffer)
        }
      }
      promise.resolve(
        digest.digest().joinToString("") { "%02x".format(it) }
      )
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun isDir(path: String, promise: Promise) {
    ioScope.launch {
      try {
        promise.resolve(parsePathToFile(path).isDirectory)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun ls(path: String, promise: Promise) {
    ioScope.launch {
      try {
        val fileList = Arguments.createArray()
        parsePathToFile(path).list()?.forEach { fileList.pushString(it) }
        promise.resolve(fileList)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun mkdir(path: String, promise: Promise) {
    ioScope.launch {
      val file = parsePathToFile(path)
      try {
        when {
          file.exists() -> {
            promise.reject("EEXIST", "'$path' already exists.")
          }
          parsePathToFile(path).mkdirs() -> {
            promise.resolve(null)
          }
          else -> {
            promise.reject("EPERM", "Failed to create directory '$path'.")
          }
        }
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun mv(source: String, target: String, promise: Promise) {
    ioScope.launch {
      try {
        if (!parsePathToFile(source).renameTo(parsePathToFile(target))) {
          parsePathToFile(source).also { it.copyTo(parsePathToFile(target), overwrite = true) }.delete()
        }
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun readFile(path: String, encoding: String, promise: Promise) {
    try {
      val data = openForReading(path).use { it.readBytes() }
      if (encoding == "base64") {
        promise.resolve(Base64.encodeToString(data, Base64.DEFAULT))
      } else {
        promise.resolve(data.decodeToString())
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun stat(path: String, promise: Promise) {
    try {
      val file = parsePathToFile(path)
      if (file.exists()) {
        promise.resolve(Arguments.makeNativeMap(mapOf(
          "filename" to file.name,
          "lastModified" to file.lastModified(),
          "path" to file.path,
          "size" to file.length(),
          "type" to if (file.isDirectory) "directory" else "file",
        )))
      } else {
        promise.reject("ENOENT", "'$path' does not exist.")
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun unlink(path: String, promise: Promise) {
    try {
      val file = parsePathToFile(path)
      if (file.exists() && file.deleteRecursively()) {
        promise.resolve(null)
      } else {
        promise.reject("ERR", "Failed to unlink '$path'.")
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun writeFile(path: String, data: String, encoding: String, promise: Promise) {
    ioScope.launch {
      try {
        if (encoding == "base64") {
          parsePathToFile(path).writeBytes(Base64.decode(data, Base64.DEFAULT))
        } else {
          parsePathToFile(path).writeText(data)
        }
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  /**
   * Open a file. Supports standard file system paths, file URIs and Storage Access
   * Framework content URIs.
   */
  private fun openForReading(path: String): InputStream {
    return if (path.startsWith("content://")) {
      reactApplicationContext.contentResolver.openInputStream(Uri.parse(path))!!
    } else {
      parsePathToFile(path).inputStream()
    }
  }

  /**
   * Return a File object and do some basic sanitization of the passed path.
   */
  private fun parsePathToFile(path: String): File {
    return if (path.contains("://")) {
      try {
        val pathUri = Uri.parse(path)
        File(pathUri.path!!)
      } catch (e: Throwable) {
        File(path)
      }
    } else {
      File(path)
    }
  }
}
