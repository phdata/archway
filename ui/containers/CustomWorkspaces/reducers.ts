import { fromJS } from 'immutable';
import { SET_CUSTOM_DESCRIPTIONS } from './actions';

const initialState = fromJS({
  customDescriptions: [],
});

const templates = (state = initialState, action: any) => {
  switch (action.type) {
    case SET_CUSTOM_DESCRIPTIONS:
      return state.set('customDescriptions', fromJS(action.customDescriptions));
    default:
      return state;
  }
};

export default templates;
