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

export const requestWorkspace =
    (token, workspace) => withBody('/workspaces', token, workspace);

export const workspaceMemberList =
    (token, id) => get(`/workspaces/${id}/members`, token);

export const workspaceNewMember =
    (token, id, db, role, username) => withBody(`/workspaces/${id}/${db}/${role}`, token, { username });

export const removeWorkspaceMember =
    (username, token, id) => withBody(`/workspaces/${id}/members/${username}`, token, {}, 'DELETE');

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
    (token, id, name, role) => get(`/workspaces/${id}/${name}/${role}`, token);
