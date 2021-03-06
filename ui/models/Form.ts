export interface QuotaForm {
  quota: number;
}

export interface CoreMemoryForm {
  core: number;
  memory: number;
}

export interface MemberForm {
  distinguishedName: string;
  roles: object;
}

export interface SimpleTopicMemberForm {
  distinguishedName: string;
  role: string;
}
