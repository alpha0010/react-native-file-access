import { NativeModules } from 'react-native';
import type {
  AssetType,
  Encoding,
  ExternalDir,
  FetchResult,
  FileStat,
  FsStat,
  HashAlgorithm,
} from './types';

type FileAccessType = {
  appendFile(path: string, data: string, encoding: Encoding): Promise<void>;
  concatFiles(source: string, target: string): Promise<number>;
  cp(source: string, target: string): Promise<void>;
  /**
   * `type` only used on Android.
   */
  cpAsset(asset: string, target: string, type?: AssetType): Promise<void>;
  cpExternal(
    source: string,
    targetName: string,
    dir: ExternalDir
  ): Promise<void>;
  df(): Promise<FsStat>;
  exists(path: string): Promise<boolean>;
  fetch(
    resource: string,
    init: {
      body?: string;
      headers?: { [key: string]: string };
      method?: string;
      path?: string;
    }
  ): Promise<FetchResult>;
  getAppGroupDir(groupName: string): Promise<string>;
  hash(path: string, algorithm: HashAlgorithm): Promise<string>;
  isDir(path: string): Promise<boolean>;
  ls(path: string): Promise<string[]>;
  mkdir(path: string): Promise<void>;
  mv(source: string, target: string): Promise<void>;
  readFile(path: string, encoding: Encoding): Promise<string>;
  stat(path: string): Promise<FileStat>;
  unlink(path: string): Promise<void>;
  writeFile(path: string, data: string, encoding: Encoding): Promise<void>;
};

export const FileAccessNative: FileAccessType = NativeModules.RNFileAccess;
