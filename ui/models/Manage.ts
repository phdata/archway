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
