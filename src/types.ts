/**
 * asset - Android `assets/` folder or iOS/MacOS main bundle.
 * resource - Android `res/` folder.
 */
export type AssetType = 'asset' | 'resource';

export type Encoding = 'utf8' | 'base64';

export type ExternalDir = 'audio' | 'downloads' | 'images' | 'video';

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

export type FetchResult = {
  /**
   * Get an HTTP response header value. (Case insensitive matching.)
   */
  getHeader: (header: string) => string | undefined;

  /**
   * Response HTTP headers.
   */
  headers: { [key: string]: string };

  /**
   * True if the response is a 2XX HTTP status.
   */
  ok: boolean;

  /**
   * Note: this value may not be accurate.
   */
  redirected: boolean;

  /**
   * HTTP response status code.
   */
  status: number;

  /**
   * Associated text for HTTP status code.
   */
  statusText: string;

  /**
   * Final URL provided by the HTTP response.
   */
  url: string;
};

export type FetchCompleteEvent = {
  requestId: number;
  state: 'complete';
} & Omit<FetchResult, 'getHeader'>;

export type FetchErrorEvent = {
  requestId: number;
  state: 'error';
  message: string;
};

export type FetchProgressEvent = {
  requestId: number;
  state: 'progress';
  bytesRead: number;
  contentLength: number;
  done: boolean;
};

export type FetchEvent =
  | FetchCompleteEvent
  | FetchErrorEvent
  | FetchProgressEvent;

/**
 * MD5 and SHA-1 are insecure. Avoid when possible.
 */
export type HashAlgorithm =
  | 'MD5'
  | 'SHA-1'
  | 'SHA-224'
  | 'SHA-256'
  | 'SHA-384'
  | 'SHA-512';

export type ManagedFetchResult = {
  cancel: () => Promise<void>;
  result: Promise<FetchResult>;
};

export type ProgressListener = (
  bytesRead: number,
  contentLength: number,
  done: boolean
) => void;
