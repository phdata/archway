export const TOKEN_EXTRACTOR = (s: any) => s.getIn(['login', 'token']);

export const RECENT_WORKSPACES_KEY = 'recentWorkspaces';
export const SPNEGO = 'spnego';
export enum featureFlagType {
  Application = 'application',
  Messaging = 'messaging',
}
