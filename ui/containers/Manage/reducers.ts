import { fromJS } from 'immutable';
import {
  SET_LOADING_STATUS,
  SET_COMPLIANCES,
  SET_SELECTED_COMPLIANCE,
  CLEAR_SELECTED_COMPLIANCE,
  SET_QUESTION,
  ADD_NEW_QUESTION,
  REMOVE_QUESTION,
  SET_LINKSGROUPS,
  SET_SELECTED_LINKSGROUP,
  ADD_NEW_LINK,
  SET_LINK,
  REMOVE_LINK,
  CLEAR_SELECTED_LINKSGROUP,
} from './actions';

const initialState = fromJS({
  compliances: [],
  selectedCompliance: {
    name: '',
    description: '',
    questions: [],
  },
  linksGroups: [],
  selectedLinksGroup: {
    name: '',
    description: '',
    links: [],
  },
  loading: true,
});

function manage(state = initialState, action: any) {
  switch (action.type) {
    case SET_LOADING_STATUS:
      return state.set('loading', action.loading);

    case SET_COMPLIANCES:
      return state.set('compliances', fromJS(action.compliances));

    case SET_SELECTED_COMPLIANCE:
      return state.set('selectedCompliance', fromJS(action.compliance));

    case CLEAR_SELECTED_COMPLIANCE:
      return state.set('selectedCompliance', initialState.get('selectedCompliance'));

    case ADD_NEW_QUESTION:
      return state.setIn(
        ['selectedCompliance', 'questions'],
        state.getIn(['selectedCompliance', 'questions']).push(action.question)
      );

    case SET_QUESTION:
      return state.setIn(['selectedCompliance', 'questions', action.objQuestion.index], action.objQuestion.question);

    case REMOVE_QUESTION:
      return state.setIn(
        ['selectedCompliance', 'questions'],
        state
          .getIn(['selectedCompliance', 'questions'])
          .slice(0, action.index)
          .concat(state.getIn(['selectedCompliance', 'questions']).slice(action.index + 1))
      );

    case SET_LINKSGROUPS:
      return state.set('linksGroups', fromJS(action.linksGroups));

    case SET_SELECTED_LINKSGROUP:
      return state.set('selectedLinksGroup', fromJS(action.linksGroup));

    case ADD_NEW_LINK:
      console.log(state.getIn(['selectedLinksGroup', 'links']));
      return state.setIn(
        ['selectedLinksGroup', 'links'],
        state.getIn(['selectedLinksGroup', 'links']).push(action.link)
      );

    case SET_LINK:
      return state.setIn(['selectedLinksGroup', 'links', action.objLink.index], action.objLink.link);

    case REMOVE_LINK:
      return state.setIn(
        ['selectedLinksGroup', 'links'],
        state
          .getIn(['selectedLinksGroup', 'links'])
          .slice(0, action.index)
          .concat(state.getIn(['selectedLinksGroup', 'links']).slice(action.index + 1))
      );

    case CLEAR_SELECTED_LINKSGROUP:
      return state.set('selectedLinksGroup', initialState.get('selectedLinksGroup'));

    default:
      return state;
  }
}

export default manage;
