import { NativeModules } from 'react-native';

type FileAccessType = {
  multiply(a: number, b: number): Promise<number>;
};

const { RNFileAccess } = NativeModules;

export default RNFileAccess as FileAccessType;
