import { fromJS } from 'immutable';
import {
  GET_WORKSPACE,
  SET_WORKSPACE,
  SET_MEMBERS,
  SET_NAMESPACE_INFO,
  SET_RESOURCE_POOLS,
} from './actions';

const initialState = fromJS({
  fetching: false,
  details: false,
});

const details = (state = initialState, action: any) => {
  switch (action.type) {

    case GET_WORKSPACE:
      return state
        .set('fetching', true);

    case SET_WORKSPACE:
      return state
        .set('fetching', false)
        .set('details', fromJS(action.workspace));

    case SET_MEMBERS:
      return state
        .set('members', fromJS(action.members));

    case SET_NAMESPACE_INFO:
      return state
        .set('namespaceInfo', fromJS(action.infos));

    case SET_RESOURCE_POOLS:
      return state
        .set('resourcePools', fromJS(action.resourcePools));

    default:
      return state;
  }
};

export default details;
