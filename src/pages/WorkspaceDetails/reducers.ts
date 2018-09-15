import { fromJS } from 'immutable';
import { GET_WORKSPACE, SET_WORKSPACE } from './actions';

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
        .set('details', fromJS(action.workspace))

    default:
      return state;
  }
}

export default details;
