import { NativeModules } from 'react-native';
import { FileAccessNative } from './native';
import type { ExternalDir, HashAlgorithm } from './types';

export type {
  ExternalDir,
  FetchResult,
  FileStat,
  FsStat,
  HashAlgorithm,
} from './types';

export const FileSystem = {
  /**
   * Append content to a file.
   */
  appendFile(path: string, data: string) {
    return FileAccessNative.appendFile(path, data);
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
   */
  cpAsset(asset: string, target: string) {
    return FileAccessNative.cpAsset(asset, target);
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
    init: {
      body?: string;
      headers?: { [key: string]: string };
      method?: string;
      /**
       * Output path.
       */
      path?: string;
    }
  ) {
    return FileAccessNative.fetch(resource, init);
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
  readFile(path: string) {
    return FileAccessNative.readFile(path);
  },

  /**
   * Read file metadata.
   */
  stat(path: string) {
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
   */
  writeFile(path: string, data: string) {
    return FileAccessNative.writeFile(path, data);
  },
};

export const Dirs: {
  CacheDir: string;
  DatabaseDir?: string;
  DocumentDir: string;
  LibraryDir?: string;
  MainBundleDir: string;
} = NativeModules.RNFileAccess?.getConstants();
