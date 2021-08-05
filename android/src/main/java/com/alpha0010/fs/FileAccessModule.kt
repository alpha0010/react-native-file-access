package com.alpha0010.fs

import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.util.Base64
import com.facebook.react.bridge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileAccessModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
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
                ContentValues().apply {
                  put(MediaStore.Audio.Media.DISPLAY_NAME, targetName)

                  if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                    // Older versions require path be specified.
                    @Suppress("DEPRECATION")
                    put(
                      MediaStore.Audio.AudioColumns.DATA,
                      File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                        targetName
                      ).absolutePath
                    )
                  }
                }
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
  fun fetch(requestId: Int, resource: String, init: ReadableMap) {
    NetworkHandler(reactApplicationContext).fetch(requestId, resource, init)
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
          parsePathToFile(source).also { it.copyTo(parsePathToFile(target), overwrite = true) }
            .delete()
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
        promise.resolve(Base64.encodeToString(data, Base64.NO_WRAP))
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
        promise.resolve(
          Arguments.makeNativeMap(
            mapOf(
              "filename" to file.name,
              "lastModified" to file.lastModified(),
              "path" to file.path,
              "size" to file.length(),
              "type" to if (file.isDirectory) "directory" else "file",
            )
          )
        )
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

  @ReactMethod
  fun zip(source: String, target: String, promise: Promise) {
    ioScope.launch {
      try {
        val inDir = parsePathToFile(source)
        var inPrefix = inDir.parentFile?.absolutePath ?: inDir.absolutePath
        if (inDir.isFile) {
          inPrefix = inDir.parentFile?.parentFile?.absolutePath ?: inPrefix
        }
        val outZip = parsePathToFile(target)
        ZipOutputStream(outZip.outputStream()).use { output ->
          for (file in inDir.walkTopDown()) {
            val entryName = file.absolutePath.removePrefix(inPrefix).removePrefix("/")
            if (file.isDirectory) {
              output.putNextEntry(ZipEntry("$entryName/"))
            } else {
              output.putNextEntry(ZipEntry(entryName))
              file.inputStream().use { it.copyTo(output) }
            }
          }
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
}
