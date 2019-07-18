import { Login } from '../../models/Login';
import { Profile } from '../../models/Profile';
import { Workspace } from '../../models/Workspace';
import { ProvisioningType } from '../../constants';

export const LOGIN_SUCCESS = 'LOGIN_SUCCESS';
export const LOGIN_REQUEST = 'LOGIN_REQUEST';
export const LOGIN_FAILURE = 'LOGIN_FAILURE';

export function login(loginForm: Login) {
  return {
    type: LOGIN_REQUEST,
    login: loginForm,
  };
}

export function loginSuccess(token: string) {
  return {
    type: LOGIN_SUCCESS,
    token,
  };
}

export function loginError(error: string) {
  return {
    type: LOGIN_FAILURE,
    error,
  };
}

export const LOGOUT_REQUEST = 'LOGOUT_REQUEST';
export const LOGOUT_SUCCESS = 'LOGOUT_SUCCESS';
export const LOGOUT_FAILURE = 'LOGOUT_FAILURE';

export function requestLogout() {
  return {
    type: LOGOUT_REQUEST,
  };
}

export const TOKEN_EXTRACTED = 'TOKEN_EXTRACTED';
export const TOKEN_NOT_AVAILABLE = 'TOKEN_NOT_AVAILABLE';

export function tokenExtracted(token: string) {
  return {
    type: TOKEN_EXTRACTED,
    token,
  };
}

export function tokenNotAvailalbe() {
  return {
    type: TOKEN_NOT_AVAILABLE,
  };
}

export const PROFILE_READY = 'PROFILE_READY';

export function profileReady(profile: Profile) {
  return {
    type: PROFILE_READY,
    profile,
  };
}

export const workspaceAvailable = (workspace: Workspace) => ({
  type: 'WORKSPACE_AVAILABLE',
  workspace,
});

export const requestWorkspace = () => ({
  type: 'WORKSPACE_REQUESTED',
});

export const profileLoading = (loading: boolean) => ({
  type: 'PROFILE_LOADED',
  loading,
});

export const SET_AUTH_TYPE = 'SET_AUTH_TYPE';

export const setAuthType = (authType: string) => ({
  type: SET_AUTH_TYPE,
  authType,
});

export const SET_PROVISIONING = 'SET_PROVISIONING';
export const setProvisioning = (provisioning: ProvisioningType) => ({
  type: SET_PROVISIONING,
  provisioning,
});
