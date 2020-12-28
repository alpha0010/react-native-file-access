package com.alpha0010

import android.os.StatFs
import android.os.Environment
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.Callback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

class FileAccessModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val httpClient = OkHttpClient()
  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun getName(): String {
    return "RNFileAccess"
  }

  override fun getConstants(): MutableMap<String, Any> {
    return hashMapOf(
      "CacheDir" to reactApplicationContext.cacheDir.absolutePath,
      "DatabaseDir" to reactApplicationContext.getDatabasePath("FileAccessProbe").parent,
      "DocumentDir" to reactApplicationContext.filesDir.absolutePath,
      "MainBundleDir" to reactApplicationContext.applicationInfo.dataDir,
      "DownloadDir", to Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
    )
  }

  @ReactMethod
  fun appendFile(path: String, data: String, promise: Promise) {
    ioScope.launch {
      try {
        File(path).appendText(data)
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun concatFiles(source: String, target: String, promise: Promise) {
    try {
      File(source).inputStream().use { input ->
        FileOutputStream(File(target), true).use {
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
        File(source).copyTo(File(target), overwrite = true)
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun cpAsset(asset: String, target: String, promise: Promise) {
    try {
      reactApplicationContext.assets.open(asset).use { assetStream ->
        File(target).outputStream().use { assetStream.copyTo(it) }
      }
      promise.resolve(null)
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
        promise.resolve(File(path).exists())
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun fetch(resource: String, init: ReadableMap, promise: Promise) {
    val request = try {
      val builder = Request.Builder().url((resource))

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

    httpClient.newCall(request).enqueue(object : Callback {
      override fun onFailure(call: Call, e: IOException) {
        promise.reject(e)
      }

      override fun onResponse(call: Call, response: Response) {
        try {
          response.use {
            if (init.hasKey("path")) {
              File(init.getString("path"))
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
      File(path).inputStream().use {
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
        promise.resolve(File(path).isDirectory)
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
        File(path).list().forEach { fileList.pushString(it) }
        promise.resolve(fileList)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun mkdir(path: String, promise: Promise) {
    ioScope.launch {
      val file = File(path)
      try {
        when {
          file.exists() -> {
            promise.reject("EEXIST", "'$path' already exists.")
          }
          File(path).mkdirs() -> {
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
        if (File(source).renameTo(File(target))) {
          promise.resolve(null)
        } else {
          promise.reject("EPERM", "Failed to rename '$source' to $target'.")
        }
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }

  @ReactMethod
  fun readFile(path: String, promise: Promise) {
    try {
      promise.resolve(File(path).readText())
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun stat(path: String, promise: Promise) {
    try {
      val file = File(path)
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
      if (File(path).delete()) {
        promise.resolve((null))
      } else {
        promise.reject("ERR", "Failed to unlink '$path'.")
      }
    } catch (e: Throwable) {
      promise.reject(e)
    }
  }

  @ReactMethod
  fun writeFile(path: String, data: String, promise: Promise) {
    ioScope.launch {
      try {
        File(path).writeText(data)
        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }
}
