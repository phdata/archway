import { ComplianceType } from '../constants';

export interface Question {
  question: string;
  requester: string;
  date: Date;
  id?: number;
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
