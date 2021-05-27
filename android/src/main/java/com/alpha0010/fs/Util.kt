package com.alpha0010.fs

import android.net.Uri
import java.io.File

/**
 * Return a File object and do some basic sanitization of the passed path.
 */
fun parsePathToFile(path: String): File {
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
