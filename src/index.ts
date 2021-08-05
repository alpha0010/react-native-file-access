import { NativeModules, Platform } from 'react-native';
import { FileAccessEventEmitter, FileAccessNative } from './native';
import type {
  AssetType,
  Encoding,
  ExternalDir,
  FetchEvent,
  FetchResult,
  FileStat,
  FsStat,
  HashAlgorithm,
} from './types';

export type {
  AssetType,
  Encoding,
  ExternalDir,
  FetchResult,
  FileStat,
  FsStat,
  HashAlgorithm,
} from './types';

/**
 * ID tracking next fetch request.
 */
let nextRequestId = 1;

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
  cp(source: string, target: string) {
    return FileAccessNative.cp(source, target);
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
    return Platform.OS === 'android'
      ? FileAccessNative.cpAsset(asset, target, type)
      : FileAccessNative.cpAsset(asset, target);
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
  df(): Promise<FsStat> {
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
  async fetch(
    resource: string,
    init: {
      body?: string;
      headers?: { [key: string]: string };
      method?: string;
      /**
       * Output path.
       */
      path?: string;
    },
    onProgress?: (
      bytesRead: number,
      contentLength: number,
      done: boolean
    ) => void
  ): Promise<FetchResult> {
    const requestId = nextRequestId++;
    return new Promise((resolve, reject) => {
      const listener = FileAccessEventEmitter.addListener(
        'FetchEvent',
        (event: FetchEvent) => {
          if (event.requestId !== requestId) {
            return;
          }

          if (event.state === 'progress') {
            onProgress?.(event.bytesRead, event.contentLength, event.done);
          } else if (event.state === 'error') {
            listener.remove();
            reject(new Error(event.message));
          } else if (event.state === 'complete') {
            listener.remove();
            resolve({
              headers: event.headers,
              ok: event.ok,
              redirected: event.redirected,
              status: event.status,
              statusText: event.statusText,
              url: event.url,
            });
          }
        }
      );
      FileAccessNative.fetch(requestId, resource, init);
    });
  },

  /**
   * Return the local storage directory for app groups.
   *
   * This is an Apple only feature.
   */
  getAppGroupDir(groupName: string) {
    if (Platform.OS !== 'ios' && Platform.OS !== 'macos') {
      return Promise.reject(
        new Error('AppGroups are available on Apple devices only')
      );
    }
    return FileAccessNative.getAppGroupDir(groupName);
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
   * Read file metadata.
   */
  stat(path: string): Promise<FileStat> {
    return FileAccessNative.stat(path);
  },

  /**
   * Delete a file.
   */
  unlink(path: string) {
    return FileAccessNative.unlink(path);
  },

  /**
   * Write content to a file.
   *
   * Default encoding of `data` is assumed utf8.
   */
  writeFile(path: string, data: string, encoding: Encoding = 'utf8') {
    return FileAccessNative.writeFile(path, data, encoding);
  },

  /**
   * Zip a file or directory.
   *
   * Note: the resulting archive will always contain a single top level
   * directory, under which all files will be located. Implementation uses
   * only platform api, which has this limitation on iOS.
   */
  zip(source: string, target: string) {
    return FileAccessNative.zip(source, target);
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
} = NativeModules.RNFileAccess?.getConstants();
