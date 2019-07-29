import { fromJS } from 'immutable';
import {
  SET_LOADING,
  SET_BEHAVIOR,
  SET_REQUEST,
  SET_TEMPLATE,
  SET_WORKSPACE,
  SET_CURRENT_PAGE,
  CREATE_WORKSPACE_REQUEST,
  CREATE_WORKSPACE_FAILURE,
  SET_ADVANCED_VISIBLE,
  CLEAR_REQUEST,
  CLEAR_ERROR,
} from './actions';
import { PAGE_BEHAVIOR } from './constants';

const initialState = fromJS({
  loading: false,
  behavior: '',
  request: {
    name: '',
    summary: '',
    description: '',
    compliance: {},
  },
  template: false,
  workspace: false,
  currentPage: PAGE_BEHAVIOR,
  advancedVisible: false,
  error: '',
});

const request = (state = initialState, action: any) => {
  switch (action.type) {
    case SET_LOADING:
      return state.set('loading', action.loading);
    case SET_BEHAVIOR:
      return state.set('behavior', action.behavior);

    case SET_REQUEST:
      return state.set('request', fromJS(action.request));

    case SET_TEMPLATE:
      return state.set('template', action.template);

    case SET_WORKSPACE:
      return state.set('workspace', fromJS(action.workspace));

    case SET_CURRENT_PAGE:
      return state.set('currentPage', action.page);

    case CREATE_WORKSPACE_REQUEST:
      return state.set('loading', true).set('error', '');

    case CREATE_WORKSPACE_FAILURE:
      return state.set('loading', false).set('error', action.error);

    case SET_ADVANCED_VISIBLE:
      return state.set('advancedVisible', action.visible);

    case CLEAR_REQUEST:
      return initialState;

    case CLEAR_ERROR:
      return state.set('error', '');

    default:
      return state;
  }
};

export default request;
