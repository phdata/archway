export interface YarnApplication {
  id: string;
  name: string;
  start_time?: Date;
}

export interface ResourcePoolsInfo {
  loading: boolean;
  error?: string;
  data: PoolInfo[];
}

export interface PoolInfo {
  name: string;
  applications: YarnApplication[];
}

export interface NamespaceInfo {
  name: string;
  tables: HiveTable[];
}

export interface HiveTable {
  name: string;
}

export interface MemberRemoveStatus {
  loading?: boolean;
  success?: boolean;
  error?: boolean;
}

export interface Member {
  username: string;
  name: string;
  removeStatus?: MemberRemoveStatus;
}

export interface Application {
  id: number;
  name: string;
  consumer_group: string;
  group: SecurityGroup;
}

export interface TopicGrant {
  id: number;
  group: SecurityGroup;
  actions: string;
  topic_access?: Date;
}

export interface KafkaTopic {
  id: number;
  name: string;
  partitions: number;
  replication_factor: number;
  managing_role: TopicGrant;
  readonly_role: TopicGrant;
}

export interface ResourcePool {
  id: number;
  pool_name: string;
  max_cores: number;
  max_memory_in_gb: number;
}

export interface SecurityGroup {
  common_name: string;
  distinguished_name: string;
  sentry_role: string;
  group_created?: Date;
  role_created?: Date;
  role_associated?: Date;
}

export interface Compliance {
  phi_data: boolean;
  pci_data: boolean;
  pii_data: boolean;
}

export interface DatabaseGrant {
  group: SecurityGroup;
  location_access?: Date;
  database_access?: Date;
}

export interface HiveAllocation {
  id: number;
  name: string;
  location: string;
  size_in_gb: number;
  consumed_in_gb: number;
  managing_group: DatabaseGrant;
  readonly_group?: DatabaseGrant;
}

export interface ApprovalStatus {
  loading?: boolean;
  success?: boolean;
  error?: string;
}

export interface ApprovalItem {
  approver?: string;
  approval_time?: string;
  status?: ApprovalStatus;
}

export interface Approvals {
  infra?: ApprovalItem;
  risk?: ApprovalItem;
}

export interface Workspace {
  id: number;
  name: string;
  summary: string;
  description: string;
  behavior: string;
  requested_date: Date;
  requester: string;
  single_user: boolean;
  status?: string;
  compliance: Compliance;
  approvals?: Approvals;
  data: HiveAllocation[];
  processing: ResourcePool[];
  topics: KafkaTopic[];
  applications: Application[];
}

export interface UserSuggestion {
  display: string;
  common_name: string;
}

export interface UserSuggestions {
  filter: string;
  users: UserSuggestion[];
  groups: UserSuggestion[];
}
