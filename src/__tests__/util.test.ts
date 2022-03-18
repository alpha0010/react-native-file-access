import { Util } from '../util';

it('extracts basename', () => {
  expect(Util.basename('')).toBe('');
  expect(Util.basename('/')).toBe('');
  expect(Util.basename('/abc/def')).toBe('def');
  expect(Util.basename('abc/')).toBe('abc');
  expect(Util.basename('abc//def/')).toBe('def');
  expect(Util.basename('/abc.def/hij.klm')).toBe('hij.klm');

  expect(Util.basename('\\abc\\def', '\\')).toBe('def');

  expect(Util.basename('/abc.def/hij.klm', '$')).toBe('/abc.def/hij.klm');
  expect(Util.basename('$abc.def$hij.klm', '$')).toBe('hij.klm');
  expect(Util.basename('abc$$def$', '$')).toBe('def');

  expect(Util.basename('abc::def:', '::')).toBe('def:');
});

it('extracts dirname', () => {
  expect(Util.dirname('')).toBe('.');
  expect(Util.dirname('/')).toBe('/');
  expect(Util.dirname('/abc/def')).toBe('/abc');
  expect(Util.dirname('abc/')).toBe('.');
  expect(Util.dirname('abc//def/')).toBe('abc');
  expect(Util.dirname('/abc.def/hij.klm')).toBe('/abc.def');
  expect(Util.dirname('/ab////cd/ef/gh.ij')).toBe('/ab/cd/ef');

  expect(Util.dirname('\\abc\\def', '\\')).toBe('\\abc');

  expect(Util.dirname('/abc.def/hij.klm', '$')).toBe('.');
  expect(Util.dirname('$abc.def$hij.klm', '$')).toBe('$abc.def');
  expect(Util.dirname('abc$$def$', '$')).toBe('abc');

  expect(Util.dirname('abc::def:', '::')).toBe('abc');
});

it('extracts extname', () => {
  expect(Util.extname('')).toBe('');
  expect(Util.extname('.hidden')).toBe('');
  expect(Util.extname('abc/def/.hidden')).toBe('');
  expect(Util.extname('abc.def')).toBe('def');
  expect(Util.extname('abc.def/')).toBe('');
  expect(Util.extname('a.b')).toBe('b');
  expect(Util.extname('/a.b')).toBe('b');
  expect(Util.extname('/abc/d.ef/g.hi.jkl')).toBe('jkl');
  expect(Util.extname('/abc/def.ghi/jkl')).toBe('');
});
