import { fromJS } from 'immutable';

import { SET_RECENT_WORKSPACES } from './actions';

const initialState = fromJS({
  recent: [],
});

function cluster(state = initialState, action: any) {
  switch (action.type) {
    case SET_RECENT_WORKSPACES:
      return state.set('recent', fromJS(action.workspaces));
    default:
      return state;
  }
}

export default cluster;
