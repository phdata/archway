export enum ShowTypes {
  Users = 'Users',
  Groups = 'Groups',
}

export const distinguishedNameRegEx = /(?=.*?(CN=))(?=.*?(OU=))(?=.*?(DC=))/is;
