/**
 * Escape for use as literal string in a regex.
 */
function regexEscape(literal: string) {
  return literal.replace(/[\^$\\.*+?()[\]{}|]/g, '\\$&');
}

/**
 * Condense consecutive separators.
 */
function normalizeSeparator(path: string, separator: string) {
  const sepRe = new RegExp(`(${regexEscape(separator)}){2,}`, 'g');
  return path.replace(sepRe, separator.replace(/\$/g, '$$$$'));
}

/**
 * Split path on last separator.
 */
function splitPath(path: string, separator: string) {
  let norm = normalizeSeparator(path, separator);
  if (norm === separator) {
    return { dir: separator, base: '' };
  }
  if (norm.endsWith(separator)) {
    norm = norm.substring(0, norm.length - separator.length);
  }
  const idx = norm.lastIndexOf(separator);
  if (idx === -1) {
    return { dir: '.', base: norm };
  }
  return {
    dir: norm.substring(0, idx),
    base: norm.substring(idx + separator.length),
  };
}

export const Util = {
  /**
   * Get the file/folder name from the end of the path.
   */
  basename(path: string, separator = '/') {
    return splitPath(path, separator).base;
  },

  /**
   * Get the path containing the file/folder.
   */
  dirname(path: string, separator = '/') {
    return splitPath(path, separator).dir;
  },

  /**
   * Get the file extension.
   */
  extname(path: string, separator = '/') {
    const extIdx = path.lastIndexOf('.');
    if (extIdx <= 0) {
      return '';
    }

    const sepIdx = path.lastIndexOf(separator);
    if (sepIdx === -1 || extIdx > sepIdx + separator.length) {
      return path.substring(extIdx + 1);
    }
    return '';
  },
};
