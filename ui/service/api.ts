import { Workspace } from '../models/Workspace';
import { ComplianceContent } from '../models/Manage';
import { SPNEGO } from '../constants';

const { config = {} } = window as any;
const BASE_URL = config.baseUrl || '';

export function login(username: string, password: string, authType: string) {
  // tslint:disable-next-line: no-shadowed-variable
  const headers =
    authType === SPNEGO ? { withCredentials: 'true' } : { Authorization: `Basic ${btoa(`${username}:${password}`)}` };

  return fetch(`${BASE_URL}/token`, {
    headers: headers as any,
  }).then(response => response.json());
}

export function logout() {
  window.localStorage.clear();
  window.location.href = '/';
}

const headers = (token?: string): RequestInit => {
  const result: string[][] = [['Accept', 'application/json'], ['Content-Type', 'application/json']];

  if (token) {
    result.push(['Authorization', token!]);
  }

  return {
    headers: result,
  };
};

const get = (path: string, token?: string, page?: string) =>
  fetch(`${BASE_URL}${path}`, headers(token)).then((response: Response) => {
    const json = response.json();
    if (response.status >= 200 && response.status < 300) {
      return json;
    } else {
      if (response.status === 403 && page === 'WorkspaceDetails') {
        throw response.status;
      } else if (response.status === 401) {
        logout();
      } else if (response.status === 404 && page === 'PersonalWorkspace') {
        throw response.status;
      }
      return json.then(Promise.reject.bind(Promise));
    }
  });

const withBody = (path: string, token: string, data?: any, method = 'POST', allow404 = false, parseResponse = true) =>
  fetch(`${BASE_URL}${path}`, {
    ...headers(token),
    method,
    body: JSON.stringify(data),
  }).then(response => {
    let result;
    if (parseResponse) {
      result = response.json();
    } else {
      result = response.text();
    }

    if (response.status >= 200 && response.status < 300) {
      return result;
    } else if (response.status === 404 && allow404) {
      return result;
    } else if (response.status === 304 && path.includes('provision' || 'deprovision')) {
      return result;
    } else {
      if (response.status === 401) {
        logout();
      }
      return result.then(Promise.reject.bind(Promise));
    }
  });

export const cluster = (token: string) => get('/clusters', token);

export const profile = (token: string) => get('/account/profile', token);

export const getPersonalWorkspace = (token: string) => get(`/account/workspace`, token, 'PersonalWorkspace');

export const getUserSuggestions = (token: string, filter: string) => get(`/members/${filter}`, token);

export const createWorkspace = (token: string) => withBody(`/account/workspace`, token);

export const requestWorkspace = (token: string, workspace: Workspace) => withBody('/workspaces', token, workspace);

export const newWorkspaceMember = (
  token: string,
  id: number,
  resource: string,
  resource_id: number,
  role: string,
  distinguished_name: string
) => withBody(`/workspaces/${id}/members`, token, { distinguished_name, resource, resource_id, role }, 'POST', true);

export const removeWorkspaceMember = (
  token: string,
  id: number,
  resource: string,
  resource_id: number,
  role: string,
  distinguished_name: string
) =>
  withBody(
    `/workspaces/${id}/members`,
    token,
    { distinguished_name, resource, resource_id, role },
    'DELETE',
    true,
    false
  );

export const getTemplate = (token: string, type: string) => get(`/templates/${type}`, token);

export const processTemplate = (token: string, type: string, input = {}) =>
  withBody(`/templates/${type}`, token, input);

export const listWorkspaces = (token: string) => get('/workspaces', token);

export const listRiskWorkspaces = (token: string) => get('/risk/workspaces', token);

export const listOpsWorkspaces = (token: string) => get('/ops/workspaces', token);

export const getWorkspace = (token: string, id: number) => get(`/workspaces/${id}`, token, 'WorkspaceDetails');

export const getProvisioning = (token: string, id: number) => get(`/workspaces/${id}/status`, token);

export const approveWorkspace = (token: string, id: number, role: string) =>
  withBody(`/workspaces/${id}/approve`, token, { role });

export const getMembers = (token: string, id: number) => get(`/workspaces/${id}/members`, token);

export const getHiveTables = (token: string, id: number) => get(`/workspaces/${id}/hive`, token);

export const getYarnApplications = (token: string, id: number) => get(`/workspaces/${id}/yarn`, token);

export const getAuthtype = () => get('/auth-type');

export const getFeatureFlags = (token: string) => get('/account/feature-flags', token);

export const getCustomDescriptions = (token: string) => get('/templates/custom', token);

export const getCompliances = (token: string) => get('/workspace/questions', token);

export const changeWorkspaceOwner = (token: string, id: number, ownerDn: string) =>
  withBody(`/workspace/${id}/owner/${ownerDn}`, token);

export const requestCompliance = (token: string, compliance: ComplianceContent) =>
  withBody('/workspace/questions', token, { compliance });

export const requestTopic = (token: string, id: number, name: string, partitions: number, replication_factor: number) =>
  withBody(`/workspaces/${id}/topics`, token, { name, partitions, replication_factor });

export const requestApplication = (
  token: string,
  id: number,
  name: string,
  application_type: string,
  logo: string,
  language: string,
  repository: string
) =>
  withBody(`/workspaces/${id}/applications`, token, {
    name,
    application_type,
    logo,
    language,
    repository,
  });

export const deleteWorkspace = (token: string, id: number) =>
  withBody(`/workspaces/${id}`, token, {}, 'DELETE', true, false);

export const deprovisionWorkspace = (token: string, id: number) => withBody(`/workspaces/${id}/deprovision`, token);

export const provisionWorkspace = (token: string, id: number) => withBody(`/workspaces/${id}/provision`, token);

export const versionInfo = (token: string) => get('/account/version', token);

export const modifyDiskQuota = (token: string, id: number, value: number) =>
  withBody(`/workspace/${id}/diskquota/${value}`, token);
