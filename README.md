# react-native-file-access

[![npm](https://img.shields.io/npm/v/react-native-file-access)](https://www.npmjs.com/package/react-native-file-access)

Filesystem access for React Native. Supports saving network requests directly
to the filesystem. Supports Android scoped storage, a requirement when targeting
API 30 or higher.

## Installation

```sh
npm install react-native-file-access
```

### Compatibility

For React Native 0.64 and older, use 1.x.x. For React Native 0.65+, use 2.x.x.

## Usage

```js
import { Dirs, FileSystem } from 'react-native-file-access';

// ...

const text = await FileSystem.readFile(Dirs.CacheDir + '/test.txt');
```

#### Directory constants.

- `Dirs.CacheDir`
- `Dirs.DatabaseDir` (Android only)
- `Dirs.DocumentDir`
- `Dirs.LibraryDir` (iOS & MacOS only)
- `Dirs.MainBundleDir`
- `Dirs.SDCardDir` (Android only)
  - Prefer `FileSystem.cpExternal()` when possible.

#### Functions.

`FileSystem.appendFile(path: string, data: string, encoding?: 'utf8' | 'base64'): Promise<void>`

- Append content to a file.
  - Default encoding of `data` is assumed utf8.

`FileSystem.concatFiles(source: string, target: string): Promise<number>`

- Append a file to another file. Returns number of bytes written.

`FileSystem.cp(source: string, target: string): Promise<void>`

- Copy a file.

`FileSystem.cpAsset(asset: string, target: string, type?: 'asset' | 'resource'): Promise<void>`

- Copy a bundled asset file.
  - Default `type` is `asset`. Prefer this when possible.
  - `resource` uses the Android `res/` folder, and inherits the associated
    naming restrictions.

`FileSystem.cpExternal(source: string, targetName: string, dir: 'audio' | 'downloads' | 'images' | 'video'): Promise<void>`

- Copy a file to an externally controlled location.
  - On Android API level < 29, may require permission WRITE_EXTERNAL_STORAGE.
  - On iOS, consider using `Dirs.DocumentDir` with `UIFileSharingEnabled`
    and `LSSupportsOpeningDocumentsInPlace` enabled.

`FileSystem.df(): Promise<{ internal_free: number, internal_total: number, external_free?: number, external_total?: number }>`

- Check device available space.

`FileSystem.exists(path: string): Promise<boolean>`

- Check if a path exists.

```
FilesSystem.fetch(
  resource: string,
  init: { body?: string, headers?: { [key: string]: string }, method?: string, path?: string },
  onProgress?: (bytesRead: number, contentLength: number, done: boolean) => void
): Promise<FetchResult>

type FetchResult = {
  headers: { [key: string]: string };
  ok: boolean;
  redirected: boolean;
  status: number;
  statusText: string;
  url: string;
}
```

- Save a network request to a file.
  - `onProgress` - Optional callback to listen to download progress. Events
    are rate limited, so do not rely on `done` becoming `true`.
    `contentLength` is only accurate if the server sends the correct headers.

```
FilesSystem.fetchManaged(
  resource: string,
  init: { body?: string, headers?: { [key: string]: string }, method?: string, path?: string },
  onProgress?: (bytesRead: number, contentLength: number, done: boolean) => void
): ManagedFetchResult

type ManagedFetchResult = {
  cancel: () => Promise<void>;
  result: Promise<FetchResult>;
}
```

- Save a network request to a file.
  - Similar to `fetch()`, with the option to cancel before completion.

`FilesSystem.getAppGroupDir(groupName: string): Promise<string>`

- Get the directory for your app group (iOS & MacOS only).
  - App groups are used on iOS/MacOS for storing content, which is shared between apps.
  - This is e.g. useful for sharing data between your iOS/MacOS app and a widget or a watch app.

`FilesSystem.hash(path: string, algorithm: 'MD5' | 'SHA-1' | 'SHA-224' | 'SHA-256' | 'SHA-384' | 'SHA-512'): Promise<string>`

- Hash the file content.

`FilesSystem.isDir(path: string): Promise<boolean>`

- Check if a path is a directory.

`FileSystem.ls(path: string): Promise<string[]>`

- List files in a directory.

`FileSystem.mkdir(path: string): Promise<void>`

- Make a new directory.

`FileSystem.mv(source: string, target: string): Promise<void>`

- Move a file.

`FileSystem.readFile(path: string, encoding?: 'utf8' | 'base64'): Promise<string>`

- Read the content of a file.
  - Default encoding of returned string is utf8.

```
FileSystem.stat(path: string): Promise<FileStat>

type FileStat = {
  filename: string;
  lastModified: number;
  path: string;
  size: number;
  type: 'directory' | 'file';
}
```

- Read file metadata.

`FileSystem.statDir(path: string): Promise<FileStat[]>`

- Read metadata of all files in a directory.

`FileSystem.unlink(path: string): Promise<void>`

- Delete a file.

`FileSystem.unzip(source: string, target: string): Promise<void>`

- Extract a zip archive.

`FileSystem.writeFile(path: string, data: string, encoding?: 'utf8' | 'base64'): Promise<void>`

- Write content to a file.
  - Default encoding of `data` is assumed utf8.

#### Utility functions.

`Util.basename(path: string, separator?: string): string`

- Get the file/folder name from the end of the path.
  - Default path `separator` is `/`.

`Util.dirname(path: string, separator?: string): string`

- Get the path containing the file/folder.
  - Default path `separator` is `/`.

`Util.extname(path: string, separator?: string): string`

- Get the file extension.
  - Default path `separator` is `/`.

## Testing

For ease of testing, this library contains a mock implementation:
[jest/react-native-file-access.ts](https://github.com/alpha0010/react-native-file-access/blob/master/jest/react-native-file-access.ts).
To use, copy it into the [`__mocks__`](https://jestjs.io/docs/en/manual-mocks#mocking-node-modules)
folder, modifying if needed.

## Alternatives

This library aims to be a modern implementation of filesystem api, using Kotlin/Swift
and latest best practices. For a more established library, consider:

- [expo-file-system](https://docs.expo.io/versions/latest/sdk/filesystem/)
  - Well supported, a good option if already using Expo.
- [react-native-blob-util](https://github.com/RonRadtke/react-native-blob-util)
  - Often a dependency of other libraries.
  - Forked from, and compatible with, the popular but deprecated [rn-fetch-blob](https://github.com/joltup/rn-fetch-blob).
- [react-native-fs](https://github.com/itinance/react-native-fs)
  - Large feature set.
  - Low maintenance, aging codebase.

For more greater control over network requests, consider
[react-native-blob-courier](https://github.com/edeckers/react-native-blob-courier).

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
