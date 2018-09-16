export interface Profile {
  name: string;
  username: string;
  permissions: {
    risk_management: boolean
    platform_operations: boolean,
  };
}
