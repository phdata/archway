export enum ShowTypes {
  Users = 'Users',
  Groups = 'Groups',
}

export const distinguishedNameRegEx = /(?=.*?(CN=))(?=.*?(OU=))(?=.*?(DC=))/i;

export enum ProtocolTypes {
  HDFS = 'hdfs',
  S3A = 's3a',
}
