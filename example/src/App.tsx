import React, { useEffect, useState } from 'react';
import { SafeAreaView, ScrollView, StyleSheet, Text, View } from 'react-native';
import { FileAccess } from 'react-native-file-access';

export function App() {
  const [info, setInfo] = useState<{ key: string; value: string }[]>([]);

  useEffect(() => {
    setInfo([
      { key: 'CacheDir', value: FileAccess.CacheDir },
      { key: 'DocumentDir', value: FileAccess.DocumentDir },
    ]);

    FileAccess.df().then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'df()', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileAccess.exists(FileAccess.CacheDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'exists(CacheDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileAccess.isDir(FileAccess.DocumentDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'isDir(DocumentDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileAccess.ls(FileAccess.DocumentDir).then((res) =>
      setInfo((prev) => {
        prev.push({ key: 'ls(DocumentDir)', value: JSON.stringify(res) });
        return prev.slice();
      })
    );

    FileAccess.writeFile(
      FileAccess.CacheDir + '/test.txt',
      'Data file in CacheDir.'
    )
      .then(() => FileAccess.readFile(FileAccess.CacheDir + '/test.txt'))
      .then((res) =>
        setInfo((prev) => {
          prev.push({
            key: 'readFile(CacheDir/test.txt)',
            value: JSON.stringify(res),
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
