import {fromJS} from 'immutable';

import {
  LOGIN_FAILURE,
  LOGIN_REQUEST,
  LOGIN_SUCCESS,
  TOKEN_EXTRACTED,
  TOKEN_NOT_AVAILABLE,
  PROFILE_READY,
} from './actions';

const initialAuthState = fromJS({
  token: false,
  error: false,
  loggingIn: false,
  loading: true,
  profile: Object
});

const auth = (state = initialAuthState, action: any) => {
  switch (action.type) {
    case LOGIN_REQUEST:
      return state.set('loggingIn', true);
    case LOGIN_SUCCESS:
      return state
              .set('loggingIn', false)
              .set('token', action.token);
    case LOGIN_FAILURE:
      return state
              .set('loggingIn', false)
              .set('error', action.error);
    case TOKEN_EXTRACTED:
      return state
              .set('loading', false)
              .set('token', action.token);
    case TOKEN_NOT_AVAILABLE:
      return state
              .set('loading', false)
              .set('token', false);
    case PROFILE_READY:
      return state
              .set('profile', action.profile);
    default:
      return state;
  }
};

export default auth;