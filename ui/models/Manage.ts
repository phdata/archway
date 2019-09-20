import { ComplianceType } from '../constants';

export interface Question {
  question: string;
  requester: string;
  updated: Date;
  id?: number;
  complianceGroupId?: number;
}

export interface ComplianceContent {
  name: ComplianceType;
  id?: number;
  description: string;
  questions: Question[];
}

export interface MemberForm {
  username: string;
  roles: object;
}

export interface Link {
  name: string;
  description: string;
  url: string;
  id?: number;
  customLinkGroupId?: number;
}

export interface LinksGroup {
  name: string;
  description: string;
  links: Link[];
  id?: number;
}
