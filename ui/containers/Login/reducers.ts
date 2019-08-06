import { fromJS } from 'immutable';
import {
  LOGIN_FAILURE,
  LOGIN_REQUEST,
  LOGIN_SUCCESS,
  PROFILE_READY,
  TOKEN_EXTRACTED,
  TOKEN_NOT_AVAILABLE,
  SET_AUTH_TYPE,
  SET_PROVISIONING,
  SET_VERSION_INFO,
  SET_WORKSPACE_FETCHED,
} from './actions';

const initialAuthState = fromJS({
  token: false,
  error: false,
  loggingIn: false,
  loading: true,
  profile: false,
  profileLoading: true,
  workspace: false,
  authType: null,
  provisioning: '',
  version: '',
  workspaceFetched: false,
});

const login = (state = initialAuthState, action: any) => {
  switch (action.type) {
    case LOGIN_REQUEST:
      return state.set('loggingIn', true).set('error', false);
    case LOGIN_SUCCESS:
      return state.set('loggingIn', false).set('token', action.token);
    case LOGIN_FAILURE:
      return state.set('loggingIn', false).set('error', action.error);
    case TOKEN_EXTRACTED:
      return state.set('loading', false).set('token', action.token);
    case TOKEN_NOT_AVAILABLE:
      return state.set('loading', false).set('token', false);
    case PROFILE_READY:
      return state.set('profile', action.profile);
    case 'WORKSPACE_AVAILABLE':
      return state.set('workspace', fromJS(action.workspace));
    case 'PROFILE_LOADED':
      return state.set('profileLoading', action.loading);
    case SET_AUTH_TYPE:
      return state.set('authType', action.authType);
    case SET_PROVISIONING:
      return state.set('provisioning', action.provisioning);
    case SET_VERSION_INFO:
      return state.set('version', action.version);
    case SET_WORKSPACE_FETCHED:
      return state.set('workspaceFetched', action.isFetched);
    default:
      return state;
  }
};

export default login;
