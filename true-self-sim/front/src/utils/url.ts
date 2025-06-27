export const isExternalUrl = (url: string): boolean => /^https?:\/\//.test(url);

export const backgroundSrc = (url: string): string =>
  isExternalUrl(url) ? url : `/background/${url}`;
