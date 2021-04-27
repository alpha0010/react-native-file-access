/**
 * asset - Android `assets/` folder or iOS main bundle.
 * resource - Android `res/` folder.
 */
export type AssetType = 'asset' | 'resource';

export type Encoding = 'utf8' | 'base64';

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
