import {
  LOGIN_FAILURE,
  LOGIN_REQUEST,
  LOGIN_SUCCESS,
  TOKEN_EXTRACTED,
  TOKEN_NOT_AVAILABLE,
  PROFILE_READY,
  LOGIN_FIELD_CHANGED,
  SET_FILTERED_LIST,
} from './actions';

const initialState = {
  loginForm: {},
  token: null,
  error: null,
  loggingIn: false,
};

const auth = (state = initialState, action) => {
  switch (action.type) {
    case LOGIN_REQUEST:
      return {
        ...state,
        loggingIn: true,
      };
    case LOGIN_SUCCESS:
      return {
        ...state,
        loggingIn: false,
        token: action.token,
      };
    case LOGIN_FAILURE:
      return {
        ...state,
        loggingIn: false,
        error: action.error,
      };
    case TOKEN_EXTRACTED:
      return {
        ...state,
        loading: false,
        token: action.token,
      };
    case TOKEN_NOT_AVAILABLE:
      return {
        ...state,
        loading: false,
        token: null,
      };
    case PROFILE_READY:
      action.profile.initials =
                action.profile.name.split(' ').map(s => s.charAt(0));
      return {
        ...state,
        profile: action.profile,
      };
    case LOGIN_FIELD_CHANGED:
      const key = Object.keys(action.field)[0];
      state.loginForm[key] = action.field[key].value;
      return state;
    default:
      return state;
  }
};

export default auth;
