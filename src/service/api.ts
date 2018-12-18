import { Workspace } from '../models/Workspace';

const BASE_URL = (window as any).config.baseUrl;

export function login(username: string, password: string) {
  return fetch(`${BASE_URL}/token`, {
    headers: {
      Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    },
  }).then((response) => response.json());
}

export function logout() {
  window.localStorage.clear();
  window.location.href = '/';
}

const headers = (token?: string): RequestInit => {
  const result: string[][] = [
    ['Accept', 'application/json'],
    ['Content-Type', 'application/json'],
  ];

  if (token) {
    result.push(['Authorization', token!]);
  }

  return {
    headers: result,
  };
};

const get = (path: string, token?: string) =>
  fetch(`${BASE_URL}${path}`, headers(token))
    .then((response: Response) => {
      const json = response.json();
      if (response.status >= 200 && response.status < 300) {
        return json;
      } else {
        if (response.status === 401) {
          logout();
        }
        return json.then(Promise.reject.bind(Promise));
      }
    });

const withBody = (path: string, token: string, data?: any, method = 'POST', allow404 = false) =>
  fetch(`${BASE_URL}${path}`, {
    ...headers(token),
    method,
    body: JSON.stringify(data),
  })
    .then((response) => {
      const json = response.json();
      if (response.status >= 200 && response.status < 300) {
        return json;
      } else if (response.status === 404 && allow404) {
        return json;
      } else {
        if (response.status === 401) {
          logout();
        }
        return json.then(Promise.reject.bind(Promise));
      }
    });

export const cluster =
  () =>
    get('/clusters');

export const profile =
  (token: string) =>
    get('/account/profile', token);

export const getPersonalWorkspace =
  (token: string) =>
    get(`/account/workspace`, token);

export const getUserSuggestions =
  (token: string, filter: string) =>
    get(`/members/${filter}`, token);

export const createWorkspace =
  (token: string) =>
    withBody(`/account/workspace`, token);

export const requestWorkspace =
  (token: string, workspace: Workspace) =>
    withBody('/workspaces', token, workspace);

export const newWorkspaceMember =
  (token: string, id: number, resource: string, resource_id: number, role: string, distinguished_name: string) =>
    withBody(`/workspaces/${id}/members`, token, { distinguished_name, resource, resource_id, role }, 'POST', true);

export const removeWorkspaceMember =
  (token: string, id: number, resource: string, resource_id: number, role: string, distinguished_name: string) =>
    withBody(`/workspaces/${id}/members`, token, { distinguished_name, resource, resource_id, role }, 'DELETE', true);

export const getTemplate =
  (token: string, type: string) =>
    get(`/templates/${type}`, token);

export const processTemplate =
  (token: string, type: string, input = {}) =>
    withBody(`/templates/${type}`, token, input);

export const listWorkspaces =
  (token: string) =>
    get('/workspaces', token);

export const getWorkspace =
  (token: string, id: number) =>
    get(`/workspaces/${id}`, token);

export const approveWorkspace =
  (token: string, id: number, role: string) =>
    withBody(`/workspaces/${id}/approve`, token, { role });

export const getMembers =
  (token: string, id: number) =>
    get(`/workspaces/${id}/members`, token);

export const getHiveTables =
  (token: string, id: number) =>
    get(`/workspaces/${id}/hive`, token);

export const getYarnApplications =
  (token: string, id: number) =>
    get(`/workspaces/${id}/yarn`, token);

export const requestTopic =
  (token: string, id: number, name: string, partitions: number, replication_factor: number) =>
    withBody(`/workspaces/${id}/topics`, token, { name, partitions, replication_factor });

export const requestApplication =
  (token: string, id: number, name: string) =>
    withBody(`/workspaces/${id}/applications`, token, { name });
