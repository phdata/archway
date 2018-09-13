export interface Application {
  id: number
  name: string
  consumer_group: string
  group: SecurityGroup
};

export interface TopicGrant {
  id: number
  group: SecurityGroup
  actions: string
  topic_access?: Date
};

export interface KafkaTopic {
  id: number
  name: string
  partitions: number
  replication_factor: number
  managing_role: TopicGrant
  readonly_role: TopicGrant
};

export interface ResourcePool {
  id: number
  pool_name: string
  max_cores: number
  max_memory_in_gb: number
};

export interface SecurityGroup {
  common_name: string
  distinguished_name: string
  sentry_role: string
  group_created?: Date
  role_created?: Date
  role_associated?: Date
};

export interface Compliance {
  phi_data: boolean
  pci_data: boolean
  pii_data: boolean
};

export interface DatabaseGrant {
  group: SecurityGroup
  location_access?: Date
  database_access?: Date
};

export interface Database {
  name: string
  location: string
  size_in_gb: number
  managing_group: DatabaseGrant
  readonly_group?: DatabaseGrant
};

export interface Workspace {
  id: number
  behavior: string
  name: string
  requested_date: Date
  requestor: string
  single_user: boolean
  compliance: Compliance
  data: Array<Database>
  processing: Array<ResourcePool>
  topics: Array<KafkaTopic>
  applications: Array<Application>
};