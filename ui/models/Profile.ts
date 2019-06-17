export interface Profile {
  name: string;
  username: string;
  distinguished_name: string;
  permissions: {
    risk_management: boolean;
    platform_operations: boolean;
  };
}
