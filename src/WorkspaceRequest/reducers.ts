import {
  SET_REQUEST,
  SET_BEHAVIOR,
  SET_WORKSPACE,
  SET_GENERATING,
  SET_PAGE_NUMBER,
  WORKSPACE_REQUESTED,
} from './actions';
import { fromJS } from 'immutable';

const initialState = fromJS({
  generating: false,
  behavior: 'simple',
  worksapce: false,
  request: false,
  requesting: false,
  page: 1,
});

const request = (state = initialState, action: any) => {
  switch (action.type) {

    case SET_REQUEST:
      return state
              .set('request', action.request);

    case SET_BEHAVIOR:
      return state
              .set('behavior', action.behavior);

    case SET_WORKSPACE:
      return state
              .set('workspace', action.workspace);

    case SET_PAGE_NUMBER:
      return state
              .set('page', action.page);

    case SET_GENERATING:
      return state
              .set('generating', action.generating);

    case WORKSPACE_REQUESTED:
      return state
              .set('requesting', true);

    default:
      return state;
  }
};

export default request;
