import {
  APPLICATION_FORM_CHANGED,
  CREATE_APPLICATION,
  APPLICATION_CREATED,
} from './actions';

import {
  SET_WORKSPACE,
} from '../WorkspaceDetails/actions';

const initialState = {
  applicationForm: {},
  creating: false,
};

const applications = (state = initialState, action) => {
  switch (action.type) {
    case APPLICATION_FORM_CHANGED:
      const key = Object.keys(action.field)[0];
      state.applicationForm[key] = action.field[key].value;
      return {
        ...state
      };
    case CREATE_APPLICATION:
      return {
        ...state,
        creating: true,
      };
    case APPLICATION_CREATED:
      return {
        ...state,
        creating: false,
        applicationForm: {},
      }
    default:
      return state;
  }
}

export default applications;