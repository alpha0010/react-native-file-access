package com.alpha0010

import android.os.StatFs
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FileAccessModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val ioScope = CoroutineScope(Dispatchers.IO)

  override fun getName(): String {
    return "RNFileAccess"
  }

  override fun getConstants(): MutableMap<String, Any> {
    return hashMapOf(
      "CacheDir" to reactApplicationContext.cacheDir.absolutePath,
      "DocumentDir" to reactApplicationContext.filesDir.absolutePath
    )
  }

  @ReactMethod
  fun cp(source: String, target: String, promise: Promise) {
    ioScope.launch {
      try {
        val sourceFile = File(source)
        val targetFile = File(target)
        sourceFile.copyTo(targetFile, overwrite = true)

        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
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
        File(path).listFiles().forEach { file -> fileList.pushString(file.absolutePath) }
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
        val file = File(path)
        file.writeText(data)

        promise.resolve(null)
      } catch (e: Throwable) {
        promise.reject(e)
      }
    }
  }
}
