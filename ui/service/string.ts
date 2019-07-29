export const escapeDoubleQuotes = (str: string) => {
  return str.slice(1, -1).replace(/\"/g, `\\"`);
};
