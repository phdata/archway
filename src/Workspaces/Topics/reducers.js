import {
  TOPIC_FORM_CHANGED,
} from './actions';

const initialState = {
  topicForm: {},
  topics: false,
};

const topics = (state = initialState, action) => {
  switch (action.type) {
    case TOPIC_FORM_CHANGED:
      const key = Object.keys(action.field)[0];
      state.topicForm[key] = action.field[key].value;
      return state;
    default:
      return state;
  }
}

export default topics;