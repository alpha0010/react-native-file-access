import type { CodegenTypes, TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type FileStat = {
  /**
   * Filename does not include the path.
   */
  filename: string;
  lastModified: number;
  path: string;
  /**
   * File size in bytes.
   */
  size: number;
  type: 'directory' | 'file';
};

/**
 * Values are in bytes.
 */
export type FsStat = {
  internal_free: number;
  internal_total: number;
  external_free?: number;
  external_total?: number;
};

export type NetworkType = 'any' | 'unmetered';

export type FetchInit = {
  body?: string;
  headers?: { [key: string]: string };
  method?: string;

  /**
   * Allowed connection. Throws if specified connection is unavailable.
   */
  network?: NetworkType;

  /**
   * Output path.
   */
  path?: string;
};

export type FetchProgressEvent = {
  requestId: number;
  bytesRead: number;
  contentLength: number;
  done: boolean;
};

export type FetchErrorEvent = { requestId: number; message: string };

export type FetchCompleteEvent = {
  requestId: number;
  headers: { [key: string]: string };
  ok: boolean;
  redirected: boolean;
  status: number;
  statusText: string;
  url: string;
};

export interface Spec extends TurboModule {
  readonly onFetchProgress: CodegenTypes.EventEmitter<FetchProgressEvent>;
  readonly onFetchError: CodegenTypes.EventEmitter<FetchErrorEvent>;
  readonly onFetchComplete: CodegenTypes.EventEmitter<FetchCompleteEvent>;

  appendFile(path: string, data: string, encoding: string): Promise<void>;
  cancelFetch(requestId: CodegenTypes.Int32): Promise<void>;
  concatFiles(source: string, target: string): Promise<number>;
  cp(source: string, target: string): Promise<void>;
  /**
   * `type` only used on Android.
   */
  cpAsset(asset: string, target: string, type: string): Promise<void>;
  cpExternal(source: string, targetName: string, dir: string): Promise<void>;
  df(): Promise<FsStat>;
  exists(path: string): Promise<boolean>;
  /**
   * Listen to `onFetch*` events from the `requestId`.
   */
  fetch(requestId: CodegenTypes.Int32, resource: string, init: FetchInit): void;
  /**
   * Only defined on iOS & MacOS.
   */
  getAppGroupDir(groupName: string): Promise<string>;
  getConstants(): {
    CacheDir: string;
    DatabaseDir?: string;
    DocumentDir: string;
    LibraryDir?: string;
    MainBundleDir: string;
    SDCardDir?: string;
  };
  hardlink(source: string, target: string): Promise<void>;
  hash(path: string, algorithm: string): Promise<string>;
  isDir(path: string): Promise<boolean>;
  ls(path: string): Promise<string[]>;
  mkdir(path: string): Promise<string>;
  mv(source: string, target: string): Promise<void>;
  readFile(path: string, encoding: string): Promise<string>;
  readFileChunk(
    path: string,
    offset: CodegenTypes.Int32,
    length: CodegenTypes.Int32,
    encoding: string
  ): Promise<string>;
  stat(path: string): Promise<FileStat>;
  statDir(path: string): Promise<FileStat[]>;
  symlink(source: string, target: string): Promise<void>;
  unlink(path: string): Promise<void>;
  unzip(source: string, target: string): Promise<void>;
  writeFile(path: string, data: string, encoding: string): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('FileAccess');
