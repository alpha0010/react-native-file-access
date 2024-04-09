package com.alpha0010.fs

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReadableMap

abstract class FileAccessSpec internal constructor(context: ReactApplicationContext) :
  ReactContextBaseJavaModule(context) {

  protected abstract fun getTypedExportedConstants(): MutableMap<String, String?>

  override fun getConstants() = getTypedExportedConstants()

  abstract fun addListener(eventType: String)
  abstract fun removeListeners(count: Double)
  abstract fun appendFile(path: String, data: String, encoding: String, promise: Promise)
  abstract fun cancelFetch(requestId: Double, promise: Promise)
  abstract fun concatFiles(source: String, target: String, promise: Promise)
  abstract fun cp(source: String, target: String, promise: Promise)
  abstract fun cpAsset(asset: String, target: String, type: String, promise: Promise)
  abstract fun cpExternal(source: String, targetName: String, dir: String, promise: Promise)
  abstract fun df(promise: Promise)
  abstract fun exists(path: String, promise: Promise)
  abstract fun fetch(requestId: Double, resource: String, init: ReadableMap)
  abstract fun getAppGroupDir(groupName: String, promise: Promise)
  abstract fun hash(path: String, algorithm: String, promise: Promise)
  abstract fun isDir(path: String, promise: Promise)
  abstract fun ls(path: String, promise: Promise)
  abstract fun mkdir(path: String, promise: Promise)
  abstract fun mv(source: String, target: String, promise: Promise)
  abstract fun readFile(path: String, encoding: String, promise: Promise)
  abstract fun readFileChunk(path: String, offset: Double, length: Double, encoding: String, promise: Promise)
  abstract fun stat(path: String, promise: Promise)
  abstract fun statDir(path: String, promise: Promise)
  abstract fun unlink(path: String, promise: Promise)
  abstract fun unzip(source: String, target: String, promise: Promise)
  abstract fun writeFile(path: String, data: String, encoding: String, promise: Promise)
}
