# react-native-file-access

Filesystem access for React Native. Supports saving network requests directly
to the filesystem.

## Installation

```sh
npm install react-native-file-access
```

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
- `Dirs.LibraryDir` (iOS only)
- `Dirs.MainBundleDir`

#### Functions.

`FileSystem.appendFile(path: string, data: string): Promise<void>`
- Append content to a file.

`FileSystem.concatFiles(source: string, target: string): Promise<number>`
- Append a file to another file. Returns number of bytes written.

`FileSystem.cp(source: string, target: string): Promise<void>`
- Copy a file.

`FileSystem.cpAsset(asset: string, target: string): Promise<void>`
- Copy a bundled asset file.

`FileSystem.df(): Promise<{ internal_free: number, internal_total: number, external_free?: number, external_total?: number }>`
- Check device available space.

`FileSystem.exists(path: string): Promise<boolean>`
- Check if a path exists.

```
FilesSystem.fetch(resource: string, init: { body?: string, headers?: { [key: string]: string }, method?: string, path?: string }): Promise<FetchResult>

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

`FileSystem.readFile(path: string): Promise<string>`
- Read the content of a file.

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

`FileSystem.unlink(path: string): Promise<void>`
- Delete a file.

`FileSystem.writeFile(path: string, data: string): Promise<void>`
- Write content to a file.

## Alternatives

- [expo-file-system](https://docs.expo.io/versions/latest/sdk/filesystem/)
- [rn-fetch-blob](https://github.com/joltup/rn-fetch-blob)
- [react-native-fs](https://github.com/itinance/react-native-fs)

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
