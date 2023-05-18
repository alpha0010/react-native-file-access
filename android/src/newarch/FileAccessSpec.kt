package com.alpha0010.fs

import com.facebook.react.bridge.ReactApplicationContext

abstract class FileAccessSpec internal constructor(context: ReactApplicationContext) :
  NativeFileAccessSpec(context) {
}
