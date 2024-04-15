import type { TurboModule } from 'react-native';
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

export interface Spec extends TurboModule {
  addListener(eventType: string): void;
  removeListeners(count: number): void;
  appendFile(path: string, data: string, encoding: string): Promise<void>;
  cancelFetch(requestId: number): Promise<void>;
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
   * Listen to `FetchEvent` events from the `requestId`.
   */
  fetch(
    requestId: number,
    resource: string,
    init: {
      body?: string;
      headers?: Object;
      method?: string;
      network?: string;
      path?: string;
    }
  ): void;
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
  hash(path: string, algorithm: string): Promise<string>;
  isDir(path: string): Promise<boolean>;
  ls(path: string): Promise<string[]>;
  mkdir(path: string): Promise<string>;
  mv(source: string, target: string): Promise<void>;
  readFile(path: string, encoding: string): Promise<string>;
  readFileChunk(
    path: string,
    offset: number,
    length: number,
    encoding: string
  ): Promise<string>;
  stat(path: string): Promise<FileStat>;
  statDir(path: string): Promise<FileStat[]>;
  unlink(path: string): Promise<void>;
  unzip(source: string, target: string): Promise<void>;
  writeFile(path: string, data: string, encoding: string): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('FileAccess');
