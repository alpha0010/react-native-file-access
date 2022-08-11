package com.alpha0010.fs

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File

/**
 * Unified process path to correct type of DocumentFile.
 */
fun String.asDocumentFile(context: Context): DocumentFile {
  if (isContentUri()) {
    try {
      val uri = Uri.parse(this)
      val dFile = if (uri.isTreeUri()) {
        // Produced by Intent.ACTION_OPEN_DOCUMENT_TREE requests.
        DocumentFile.fromTreeUri(context, uri)
      } else {
        // Produced by Intent.ACTION_CREATE_DOCUMENT and
        // Intent.ACTION_OPEN_DOCUMENT requests.
        DocumentFile.fromSingleUri(context, uri)
      }
      if (dFile != null) {
        return dFile
      }
    } catch (e: Throwable) {
      // Ignored.
    }
  }
  // Regular (app internal) filesystem path.
  return DocumentFile.fromFile(parsePathToFile(this))
}

/**
 * Check if the string looks like it is a scoped storage content uri.
 */
fun String.isContentUri() = startsWith("content://")

/**
 * Assumes uri is a content uri.
 */
fun Uri.isTreeUri() = pathSegments.firstOrNull() == "tree"

/**
 * Split the last component from a scoped storage uri.
 */
fun parseScopedPath(path: String): Pair<Uri, String> {
  val uri = Uri.parse(path)
  val lastSegment = uri.lastPathSegment?.trimEnd('/')
    ?: throw Exception("Failed to parse '$path'.")
  val index = lastSegment.lastIndexOf('/')
  if (index < 1) throw Exception("Failed to parse '$path'.")
  val leading = lastSegment.substring(0, index)
  val trailing = lastSegment.substring(index + 1, lastSegment.length)
  val newUri = uri.buildUpon().path("").apply {
    for (segment in uri.pathSegments.dropLast(1)) {
      appendPath(segment)
    }
    appendPath(leading)
  }.build()
  return Pair(newUri, Uri.decode(trailing))
}

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
