# react-native-file-access

Filesystem access for React Native. Supports saving network requests directly
to the filesystem.

## Installation

```sh
npm install react-native-file-access
```

## Usage

```js
import { FileAccess } from 'react-native-file-access';

// ...

const text = await FileAccess.readFile(FileAccess.CacheDir + '/test.txt');
```

## Alternatives

- [expo-file-system](https://docs.expo.io/versions/latest/sdk/filesystem/)
- [rn-fetch-blob](https://github.com/joltup/rn-fetch-blob)
- [react-native-fs](https://github.com/itinance/react-native-fs)

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
