const BASE_URL = window.config.baseUrl;

export function login(username, password) {
  return fetch(`${BASE_URL}/token`, {
    headers: {
      Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    },
  }).then(response => response.json());
}

export function logout() {
  window.localStorage.clear();
}

const headers = token => ({
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: token,
  },
});


function get(path, token) {
  return fetch(`${BASE_URL}${path}`, {
    ...headers(token),
  }).then(response => response.json());
}

function withBody(path, token, data, method = 'POST') {
  return fetch(`${BASE_URL}${path}`, {
    ...headers(token),
    method,
    body: JSON.stringify(data),
  }).then(response => response.json());
}

export const cluster =
  () => get('/clusters', null);

export const profile =
  token => get('/account/profile', token);

  export const getPersonalWorkspace =
    (token) => get(`/account/workspace`, token);

export const createWorkspace =
  (token) => withBody(`/account/workspace`, token);

export const requestWorkspace =
  (token, workspace) => withBody('/workspaces', token, workspace);

export const newWorkspaceMember =
  (token, id, resource, resource_id, role, username) => withBody(`/workspaces/${id}/members`, token, { username, resource, resource_id, role });

export const removeWorkspaceMember =
  (token, id, resource, resource_id, role, username) => withBody(`/workspaces/${id}/members`, token, { username, resource, resource_id, role }, 'DELETE');

export const getTemplate =
  (token, type) => get(`/templates/${type}`, token);

export const processTemplate =
  (token, type, input = {}) => withBody(`/templates/${type}`, token, input);

export const listWorkspaces =
  token => get('/workspaces', token);

export const getWorkspace =
  (token, id) => get(`/workspaces/${id}`, token);

export const approveWorkspace =
  (token, id, role) => withBody(`/workspaces/${id}/approve`, token, { role });

export const getMembers =
  (token, id) => get(`/workspaces/${id}/members`, token);

export const requestTopic =
  (token, id, name, partitions, replication_factor) => withBody(`/workspaces/${id}/topics`, token, { name, partitions, replication_factor });

export const requestApplication =
  (token, id, name) => withBody(`/workspaces/${id}/applications`, token, { name });