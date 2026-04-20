import FileAccessNative, { type FetchInit } from './NativeFileAccess';
import type {
  AssetType,
  Encoding,
  ExternalDir,
  FetchResult,
  HashAlgorithm,
  ManagedFetchResult,
  ProgressListener,
} from './types';

export type {
  AssetType,
  Encoding,
  ExternalDir,
  FetchResult,
  HashAlgorithm,
} from './types';
export type { FileStat, FsStat, NetworkType } from './NativeFileAccess';

export { AndroidScoped, Util } from './util';

/**
 * ID tracking next fetch request.
 */
const getRequestId = (() => {
  let nextId = 0;
  return () => ++nextId;
})();

/**
 * Process fetch events for the request.
 */
function registerFetchListener(
  requestId: number,
  resolve: (res: FetchResult) => void,
  reject: (e: Error) => void,
  onProgress?: ProgressListener
) {
  const progressListener =
    onProgress == null
      ? null
      : FileAccessNative.onFetchProgress((event) => {
          if (event.requestId === requestId) {
            onProgress(event.bytesRead, event.contentLength, event.done);
          }
        });
  const errorListener = FileAccessNative.onFetchError((event) => {
    if (event.requestId === requestId) {
      unsubscribe();
      reject(new Error(event.message));
    }
  });
  const completeListener = FileAccessNative.onFetchComplete((event) => {
    if (event.requestId === requestId) {
      unsubscribe();
      const headersLower = new Map<string, string>();
      for (const [key, value] of Object.entries(event.headers)) {
        headersLower.set(key.toLowerCase(), value);
      }
      resolve({
        getHeader: (header: string) => headersLower.get(header.toLowerCase()),
        headers: event.headers,
        ok: event.ok,
        redirected: event.redirected,
        status: event.status,
        statusText: event.statusText,
        url: event.url,
      });
    }
  });

  const unsubscribe = () => {
    completeListener.remove();
    errorListener.remove();
    progressListener?.remove();
  };
}

/**
 * Get a `Promise` that will resolve after the specified timespan.
 */
function sleep(milliseconds: number) {
  return new Promise<void>((resolve) => setTimeout(resolve, milliseconds));
}

/**
 * Periodically report file copy status to the progress listener.
 */
async function wrapCpListener(
  source: string,
  target: string,
  completion: Promise<void>,
  onProgress: ProgressListener
) {
  const sourceStat = await FileSystem.stat(source);
  while (true) {
    const targetStat = await Promise.race([
      completion.then(() => true),
      sleep(150)
        .then(() => FileSystem.stat(target))
        .catch(() => false),
    ]);
    if (targetStat === true) {
      onProgress(sourceStat.size, sourceStat.size, true);
      break; // Process completed.
    } else if (targetStat !== false) {
      onProgress(targetStat.size, sourceStat.size, false);
    }
  }
}

export const FileSystem = {
  /**
   * Append content to a file.
   *
   * Default encoding of `data` is assumed utf8.
   */
  appendFile(path: string, data: string, encoding: Encoding = 'utf8') {
    return FileAccessNative.appendFile(path, data, encoding);
  },

  /**
   * Append a file to another file.
   *
   * Returns number of bytes written.
   */
  concatFiles(source: string, target: string) {
    return FileAccessNative.concatFiles(source, target);
  },

  /**
   * Copy a file.
   */
  cp(source: string, target: string, onProgress?: ProgressListener) {
    const res = FileAccessNative.cp(source, target);
    return onProgress == null
      ? res
      : wrapCpListener(source, target, res, onProgress);
  },

  /**
   * Copy a bundled asset file.
   *
   * When using Android asset type 'resource', include the folder, but skip the
   * file extension. For example use 'raw/foo', for the file 'res/raw/foo.txt'.
   * When possible, prefer using the 'assets/' folder; files in 'res/' have
   * naming restrictions imposed by Android.
   * https://developer.android.com/guide/topics/resources/providing-resources.html#OriginalFiles
   */
  cpAsset(asset: string, target: string, type: AssetType = 'asset') {
    return FileAccessNative.cpAsset(asset, target, type);
  },

  /**
   * Copy a file to an externally controlled location.
   *
   * On Android API level < 29, may require permission WRITE_EXTERNAL_STORAGE.
   */
  cpExternal(source: string, targetName: string, dir: ExternalDir) {
    return FileAccessNative.cpExternal(source, targetName, dir);
  },

  /**
   * Check device available space.
   */
  df() {
    return FileAccessNative.df();
  },

  /**
   * Check if a path exists.
   */
  exists(path: string) {
    return FileAccessNative.exists(path);
  },

  /**
   * Save a network request to a file.
   */
  fetch(
    resource: string,
    init: FetchInit,
    onProgress?: ProgressListener
  ): Promise<FetchResult> {
    const requestId = getRequestId();
    return new Promise((resolve, reject) => {
      registerFetchListener(requestId, resolve, reject, onProgress);
      FileAccessNative.fetch(requestId, resource, init);
    });
  },

  /**
   * Save a network request to a file.
   */
  fetchManaged(
    resource: string,
    init: FetchInit,
    onProgress?: ProgressListener
  ): ManagedFetchResult {
    const requestId = getRequestId();
    return {
      cancel: () => FileAccessNative.cancelFetch(requestId),
      result: new Promise((resolve, reject) => {
        registerFetchListener(requestId, resolve, reject, onProgress);
        FileAccessNative.fetch(requestId, resource, init);
      }),
    };
  },

  /**
   * Return the local storage directory for app groups.
   *
   * This is an Apple only feature.
   */
  getAppGroupDir(groupName: string) {
    return FileAccessNative.getAppGroupDir(groupName);
  },

  /**
   * Create a hard link.
   *
   * Creates a hard link at target pointing to source.
   */
  hardlink(source: string, target: string) {
    return FileAccessNative.hardlink(source, target);
  },

  /**
   * Hash the file content.
   */
  hash(path: string, algorithm: HashAlgorithm) {
    return FileAccessNative.hash(path, algorithm);
  },

  /**
   * Check if a path is a directory.
   */
  isDir(path: string) {
    return FileAccessNative.isDir(path);
  },

  /**
   * List files in a directory.
   */
  ls(path: string) {
    return FileAccessNative.ls(path);
  },

  /**
   * Make a new directory.
   *
   * Returns path of the created directory.
   */
  mkdir(path: string) {
    return FileAccessNative.mkdir(path);
  },

  /**
   * Move a file.
   */
  mv(source: string, target: string) {
    return FileAccessNative.mv(source, target);
  },

  /**
   * Read the content of a file.
   */
  readFile(path: string, encoding: Encoding = 'utf8') {
    return FileAccessNative.readFile(path, encoding);
  },

  /**
   * Read a chunk of the content of a file.
   */
  readFileChunk(
    path: string,
    offset: number,
    length: number,
    encoding: Encoding = 'utf8'
  ) {
    return FileAccessNative.readFileChunk(path, offset, length, encoding);
  },

  /**
   * Read file metadata.
   */
  stat(path: string) {
    return FileAccessNative.stat(path);
  },

  /**
   * Read metadata of all files in a directory.
   */
  statDir(path: string) {
    return FileAccessNative.statDir(path);
  },

  /**
   * Create a symbolic link.
   *
   * Creates a symbolic link at target pointing to source.
   */
  symlink(source: string, target: string) {
    return FileAccessNative.symlink(source, target);
  },

  /**
   * Delete a file.
   */
  unlink(path: string) {
    return FileAccessNative.unlink(path);
  },

  /**
   * Extract a zip archive.
   */
  unzip(source: string, target: string) {
    return FileAccessNative.unzip(source, target);
  },

  /**
   * Write content to a file.
   *
   * Default encoding of `data` is assumed utf8.
   */
  writeFile(path: string, data: string, encoding: Encoding = 'utf8') {
    return FileAccessNative.writeFile(path, data, encoding);
  },
};

/**
 * Directory constants.
 */
export const Dirs: {
  /**
   * Temporary files. System/user may delete these if device storage is low.
   */
  CacheDir: string;

  /**
   * System recommended location for SQLite files.
   *
   * Android only.
   */
  DatabaseDir?: string;

  /**
   * Persistent data. Generally user created content.
   */
  DocumentDir: string;

  /**
   * Persistent app internal data.
   *
   * iOS & MacOS only.
   */
  LibraryDir?: string;

  /**
   * App's default root directory.
   */
  MainBundleDir: string;

  /**
   * Root path to removable media. Prefer `cpExternal()` when possible, as
   * Android discourages this access method.
   *
   * Android only.
   */
  SDCardDir?: string;
} = FileAccessNative.getConstants();
