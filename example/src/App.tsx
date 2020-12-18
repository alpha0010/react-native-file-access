import React, { useEffect, useState } from 'react';
import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Dirs, FileSystem } from 'react-native-file-access';

export function App() {
  const [info, setInfo] = useState<{ key: string; value: string }[]>([]);

  useEffect(() => {
    setInfo([
      { key: 'CacheDir', value: Dirs.CacheDir },
      { key: 'DatabaseDir', value: Dirs.DatabaseDir ?? '<undefined>' },
      { key: 'DocumentDir', value: Dirs.DocumentDir },
      { key: 'LibraryDir', value: Dirs.LibraryDir ?? '<undefined>' },
      { key: 'MainBundleDir', value: Dirs.MainBundleDir },
    ]);

    FileSystem.df().then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'df()', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileSystem.exists(Dirs.CacheDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'exists(CacheDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileSystem.isDir(Dirs.DocumentDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'isDir(DocumentDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileSystem.ls(Dirs.MainBundleDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'ls(MainBundleDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileSystem.writeFile(Dirs.CacheDir + '/test.txt', 'Data file in CacheDir.')
      .then(() => FileSystem.readFile(Dirs.CacheDir + '/test.txt'))
      .then((res) =>
        setInfo((prev) => {
          prev.push({
            key: 'readFile(CacheDir/test.txt)',
            value: JSON.stringify(res),
          });
          return prev.slice();
        })
      );

    FileSystem.fetch('https://example.com', {
      path: Dirs.CacheDir + '/download.html',
    })
      .then((res) => {
        setInfo((prev) => {
          prev.push({
            key: 'fetch(https://example.com)',
            value: JSON.stringify(res),
          });
          return prev.slice();
        });
        return FileSystem.readFile(Dirs.CacheDir + '/download.html');
      })
      .then((res) =>
        setInfo((prev) => {
          prev.push({
            key: 'fetch(https://example.com)',
            value: res.substring(0, 64),
          });
          return prev.slice();
        })
      );
  }, [setInfo]);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView>
        {info.map((value, index) => (
          <View key={`${index}-${value.key}`} style={styles.row}>
            <Text style={styles.key}>{value.key}</Text>
            <Text style={styles.value}>{value.value}</Text>
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  key: { flex: 1, padding: 2 },
  row: { flexDirection: 'row', paddingVertical: 2 },
  value: { flex: 4, padding: 2 },
});
