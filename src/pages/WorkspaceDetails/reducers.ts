import { fromJS } from 'immutable';
import {
  GET_WORKSPACE,
  SET_WORKSPACE,
  UPDATE_SELECTED_ALLOCATION,
  SET_MEMBERS,
  SET_NAMESPACE_INFO,
  SET_RESOURCE_POOLS,
  SET_ACTIVE_MODAL,
  REQUEST_APPROVAL,
  APPROVAL_SUCCESS,
  TOPIC_REQUEST_SUCCESS,
  SIMPLE_MEMBER_REQUEST_COMPLETE,
  APPROVAL_FAILURE,
  REQUEST_REMOVE_MEMBER,
  REMOVE_MEMBER_SUCCESS,
  REMOVE_MEMBER_FAILURE,
  REQUEST_REFRESH_YARN_APPS,
  REFRESH_YARN_APPS_SUCCESS,
  REFRESH_YARN_APPS_FAILURE,
} from './actions';
import { Member } from '../../types/Workspace';

const initialState = fromJS({
  fetching: false,
  details: false,
  activeModal: false,
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

    case UPDATE_SELECTED_ALLOCATION:
      return state
        .set('selectedAllocation', fromJS(action.allocation));

    case SET_MEMBERS:
      return state
        .set('members', fromJS(action.members));

    case SET_NAMESPACE_INFO:
      return state
        .set('namespaceInfo', fromJS(action.infos));

    case SET_RESOURCE_POOLS:
      return state
        .set('resourcePools', fromJS({
          loading: false,
          data: action.resourcePools,
        }));

    case SET_ACTIVE_MODAL:
      return state
          .set('activeModal', action.activeModal);

    case REQUEST_APPROVAL:
      return state
        .set(
          'details',
          state
            .get('details')
            .setIn(['approvals', action.approvalType, 'status'], {
              loading: true,
            }));

    case APPROVAL_SUCCESS:
      return state
          .set(
            'details',
            state
              .get('details')
              .setIn(['approvals', action.approvalType], {
                ...action.approval[action.approvalType],
                status: {
                  success: true,
                },
              }));

    case APPROVAL_FAILURE:
      return state
          .set(
            'details',
            state
              .get('details')
              .setIn(['approvals', action.approvalType, 'status'], {
                success: false,
                error: action.error,
              }));

    case TOPIC_REQUEST_SUCCESS:
      return state
              .set('activeModal', false);

    case SIMPLE_MEMBER_REQUEST_COMPLETE:
      return state
              .set('activeModal', false);

    case REQUEST_REMOVE_MEMBER:
      return state
          .set(
            'members',
            state
              .get('members')
              .map((m: any) => (m.toJS() as Member).username === action.username ?
                m.set('removeStatus', {
                  loading: true,
                }) : m,
              ));

    case REMOVE_MEMBER_SUCCESS:
      return state
          .set(
            'members',
            state
              .get('members')
              .map((m: any) => (m.toJS() as Member).username === action.username ?
                m.set('removeStatus', {
                  success: true,
                }) : m,
              ));

    case REMOVE_MEMBER_FAILURE:
      return state
          .set(
            'members',
            state
              .get('members')
              .map((m: any) => (m.toJS() as Member).username === action.username ?
                m.set('removeStatus', {
                  success: false,
                  error: action.error,
                }) : m,
              ));

    case REQUEST_REFRESH_YARN_APPS:
      return state
        .set(
          'resourcePools',
          state
            .get('resourcePools')
            .setIn(['loading'], true)
            .setIn(['error'], ''));

    case REFRESH_YARN_APPS_SUCCESS:
      return state
        .set(
          'resourcePools', fromJS({
            loading: false,
            data: action.apps,
          }));

    case REFRESH_YARN_APPS_FAILURE:
      return state
        .set(
          'resourcePools',
          state
            .get('resourcePools')
            .setIn(['loading'], false)
            .setIn(['error'], action.error));

    default:
      return state;
  }
};

export default details;
