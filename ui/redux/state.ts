import { Workspace } from '../models/Workspace';

export interface LoginState {
  token: boolean;
  error: boolean;
  loggingIn: boolean;
  loading: boolean;
  profile: boolean;
  profileLoading: boolean;
  workspace?: Workspace;
}

export interface ListingState {
  fetching: boolean;
  workspaces: any;
  filters: {
    filter: string;
    behaviors: string[];
    statuses: string[];
  };
}

export interface RiskState {
  fetching: boolean;
  workspaces: any;
}

export interface OpsState {
  fetching: boolean;
  workspaces: any;
}

export interface RequestState {
  generating: boolean;
  behavior: boolean;
  worksapce: boolean;
  request: boolean;
  requesting: boolean;
  template: boolean;
  page: number;
}

export interface DetailsState {
  fetching: boolean;
  details: boolean;
}

export interface IState {
  login: LoginState;
  listing: ListingState;
  risk: RiskState;
  operations: OpsState;
  request: RequestState;
  details: DetailsState;
}
