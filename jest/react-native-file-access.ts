/* global jest */

import type {
  FetchResult,
  FileStat,
  FsStat,
  HashAlgorithm,
} from 'react-native-file-access';

export const Dirs = {
  CacheDir: '/mock/CacheDir',
  DatabaseDir: '/mock/DatabaseDir',
  DocumentDir: '/mock/DocumentDir',
  LibraryDir: '/mock/LibraryDir',
  MainBundleDir: '/mock/MainBundleDir',
};

class FileSystemMock {
  /**
   * Data store for mock filesystem.
   */
  public filesystem = new Map<string, string>();

  /**
   * Append content to a file.
   */
  public appendFile = jest.fn(async (path: string, data: string) => {
    this.filesystem.set(path, (this.filesystem.get(path) ?? '') + data);
  });

  /**
   * Append a file to another file.
   *
   * Returns number of bytes written.
   */
  public concatFiles = jest.fn(async (source: string, target: string) => {
    const data = this.getFileOrThrow(source);
    this.filesystem.set(target, (this.filesystem.get(target) ?? '') + data);
    return data.length;
  });

  /**
   * Copy a file.
   */
  public cp = jest.fn(async (source: string, target: string) => {
    this.filesystem.set(target, this.getFileOrThrow(source));
  });

  /**
   * Copy a bundled asset file.
   */
  public cpAsset = jest.fn(async (asset: string, target: string) => {
    this.filesystem.set(target, `[Mock asset data for '${asset}']`);
  });

  /**
   * Check device available space.
   */
  public df = jest.fn<Promise<FsStat>, []>(async () => ({
    internal_free: 100,
    internal_total: 200,
  }));

  /**
   * Check if a path exists.
   */
  public exists = jest.fn(async (path: string) => this.filesystem.has(path));

  /**
   * Save a network request to a file.
   */
  public fetch = jest.fn(
    async (
      resource: string,
      init: {
        body?: string;
        headers?: { [key: string]: string };
        method?: string;
        path?: string;
      }
    ): Promise<FetchResult> => {
      if (init.path != null) {
        this.filesystem.set(init.path, `[Mock fetch data for '${resource}']`);
      }
      return {
        headers: {},
        ok: true,
        redirected: false,
        status: 200,
        statusText: 'OK',
        url: resource,
      };
    }
  );

  /**
   * Hash the file content.
   */
  public hash = jest.fn(async (path: string, algorithm: HashAlgorithm) => {
    if (!this.filesystem.has(path)) {
      throw new Error(`File ${path} not found`);
    }
    return `[${algorithm} hash of '${path}']`;
  });

  /**
   * Check if a path is a directory.
   */
  public isDir = jest.fn(async (path: string) => !this.filesystem.has(path));

  /**
   * List files in a directory.
   */
  public ls = jest.fn(async (_path: string) => ['file1', 'file2']);

  /**
   * Move a file.
   */
  public mv = jest.fn(async (source: string, target: string) => {
    this.filesystem.set(target, this.getFileOrThrow(source));
    this.filesystem.delete(source);
  });

  /**
   * Read the content of a file.
   */
  public readFile = jest.fn(async (path: string) => this.getFileOrThrow(path));

  /**
   * Read file metadata.
   */
  public stat = jest.fn(
    async (path: string): Promise<FileStat> => ({
      filename: path.substring(path.lastIndexOf('/')),
      lastModified: 1,
      path: path,
      size: this.getFileOrThrow(path).length,
      type: 'file',
    })
  );

  /**
   * Delete a file.
   */
  public unlink = jest.fn(async (path: string) => {
    this.filesystem.delete(path);
  });

  /**
   * Write content to a file.
   */
  public writeFile = jest.fn(async (path: string, data: string) => {
    this.filesystem.set(path, data);
  });

  private getFileOrThrow(path: string): string {
    const data = this.filesystem.get(path);
    if (data == null) {
      throw new Error(`File ${path} not found`);
    }
    return data;
  }
}

export const FileSystem = new FileSystemMock();
