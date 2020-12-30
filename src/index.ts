import { NativeModules } from 'react-native';

export type ExternalDir = 'audio' | 'downloads' | 'images' | 'video';

export type FetchResult = {
  headers: { [key: string]: string };
  ok: boolean;
  redirected: boolean;
  status: number;
  statusText: string;
  url: string;
};

export type FileStat = {
  filename: string;
  lastModified: number;
  path: string;
  size: number;
  type: 'directory' | 'file';
};

export type FsStat = {
  internal_free: number;
  internal_total: number;
  external_free?: number;
  external_total?: number;
};

export type HashAlgorithm =
  | 'MD5'
  | 'SHA-1'
  | 'SHA-224'
  | 'SHA-256'
  | 'SHA-384'
  | 'SHA-512';

type FileAccessType = {
  /**
   * Append content to a file.
   */
  appendFile(path: string, data: string): Promise<void>;

  /**
   * Append a file to another file.
   *
   * Returns number of bytes written.
   */
  concatFiles(source: string, target: string): Promise<number>;

  /**
   * Copy a file.
   */
  cp(source: string, target: string): Promise<void>;

  /**
   * Copy a bundled asset file.
   */
  cpAsset(asset: string, target: string): Promise<void>;

  /**
   * Copy a file to an externally controlled location.
   *
   * On Android API level < 29, may require permission WRITE_EXTERNAL_STORAGE.
   */
  cpExternal(
    source: string,
    targetName: string,
    dir: ExternalDir
  ): Promise<void>;

  /**
   * Check device available space.
   */
  df(): Promise<FsStat>;

  /**
   * Check if a path exists.
   */
  exists(path: string): Promise<boolean>;

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
  ): Promise<FetchResult>;

  /**
   * Hash the file content.
   */
  hash(path: string, algorithm: HashAlgorithm): Promise<string>;

  /**
   * Check if a path is a directory.
   */
  isDir(path: string): Promise<boolean>;

  /**
   * List files in a directory.
   */
  ls(path: string): Promise<string[]>;

  /**
   * Make a new directory.
   */
  mkdir(path: string): Promise<void>;

  /**
   * Move a file.
   */
  mv(source: string, target: string): Promise<void>;

  /**
   * Read the content of a file.
   */
  readFile(path: string): Promise<string>;

  /**
   * Read file metadata.
   */
  stat(path: string): Promise<FileStat>;

  /**
   * Delete a file.
   */
  unlink(path: string): Promise<void>;

  /**
   * Write content to a file.
   */
  writeFile(path: string, data: string): Promise<void>;
};

export const Dirs: {
  CacheDir: string;
  DatabaseDir?: string;
  DocumentDir: string;
  LibraryDir?: string;
  MainBundleDir: string;
} = NativeModules.RNFileAccess?.getConstants();

export const FileSystem: FileAccessType = NativeModules.RNFileAccess;
