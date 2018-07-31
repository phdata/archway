import {
  TOPIC_FORM_CHANGED,
  CREATE_TOPIC,
  TOPIC_CREATED,
} from './actions';

import {
  SET_WORKSPACE,
} from '../WorkspaceDetails/actions';

const initialState = {
  topicForm: {},
  topics: false,
  creating: false,
};

const topics = (state = initialState, action) => {
  switch (action.type) {
    case TOPIC_FORM_CHANGED:
      const key = Object.keys(action.field)[0];
      state.topicForm[key] = action.field[key].value;
      return {
        ...state
      };
    case CREATE_TOPIC:
      return {
        ...state,
        creating: true,
      };
    case TOPIC_CREATED:
      return {
        ...state,
        creating: false,
        topicForm: {},
      }
    default:
      return state;
  }
}

export default topics;