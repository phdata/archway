export const TOKEN_EXTRACTOR = (s: any) => s.getIn(['login', 'token']);

export const RECENT_WORKSPACES_KEY = 'recentWorkspaces';
export const SPNEGO = 'spnego';
export const workspaceStatuses = ['approved', 'pending', 'rejected'];
export const workspaceBehaviors = ['simple', 'structured', 'custom'];
export const behaviorProperties = {
  simple: {
    title: 'Simple',
    icon: 'team',
  },
  structured: {
    title: 'Structured',
    icon: 'deployment-unit',
  },
  custom: {
    title: 'Custom',
    icon: 'select',
  },
};

export enum FeatureFlagType {
  Application = 'application',
  Messaging = 'messaging',
  DiskQuota = 'disk-quota',
}

export enum ProvisioningType {
  Pending = 'pending',
  Complete = 'complete',
}

export enum ModalType {
  DeprovisionWorkspace = 'deprovisionWorkspace',
  ProvisionWorkspace = 'provisionWorkspace',
  DeleteWorkspace = 'deleteWorkspace',
  Application = 'application',
  Kafka = 'kafka',
  SimpleTopicMember = 'simpleTopicMember',
  SimpleMember = 'simpleMember',
  ChangeOwner = 'changeOwner',
  ModifyDiskQuota = 'modifyDiskQuota',
}

export enum NotificationType {
  Success = 'success',
  Info = 'info',
  Warning = 'warning',
  Error = 'error',
}

export enum ComplianceType {
  PCI = 'pci',
  PII = 'pii',
  PHI = 'phi',
}

export enum LinksGroupCardPage {
  Overview = 'home',
  Manage = 'manage',
}
