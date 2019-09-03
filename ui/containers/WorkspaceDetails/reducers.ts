import { fromJS } from 'immutable';
import {
  CLEAR_DETAILS,
  GET_WORKSPACE,
  SET_WORKSPACE,
  SET_USER_SUGGESTIONS,
  UPDATE_SELECTED_ALLOCATION,
  SET_MEMBERS,
  SET_NAMESPACE_INFO,
  SET_RESOURCE_POOLS,
  SET_ACTIVE_TOPIC,
  SET_ACTIVE_APPLICATION,
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
  REQUEST_REFRESH_HIVE_TABLES,
  REFRESH_HIVE_TABLES_SUCCESS,
  REFRESH_HIVE_TABLES_FAILURE,
  SET_MEMBER_LOADING,
  SET_PROVISIONING_STATUS,
  MANAGE_LOADING,
  SET_NOTIFICATION_STATUS,
  CLEAR_NOTIFICATION_STATUS,
  SET_WORKSPACE_FETCHING,
  SET_USERSUGGESTIONS_LOADING,
  SET_OWNER_LOADING,
  SET_QUOTA_LOADING,
} from './actions';

import { Member } from '../../models/Workspace';

const initialState = fromJS({
  fetching: false,
  details: false,
  activeModal: false,
  notification: {
    type: '',
    message: '',
  },
  memberLoading: false,
  provisioning: '',
  manageLoading: {
    provision: false,
    deprovision: false,
    delete: false,
  },
  userSuggestionsLoading: false,
  ownerLoading: false,
  quotaLoading: false,
});

const details = (state = initialState, action: any) => {
  switch (action.type) {
    case CLEAR_DETAILS:
      return initialState;

    case GET_WORKSPACE:
      return state.set('fetching', true);

    case SET_WORKSPACE:
      return state.set('fetching', false).set('details', fromJS(action.workspace));

    case SET_USER_SUGGESTIONS:
      return state.set(
        'userSuggestions',
        fromJS({
          filter: action.filter,
          ...action.suggestions,
        })
      );

    case UPDATE_SELECTED_ALLOCATION:
      return state.set('selectedAllocation', fromJS(action.allocation));

    case SET_MEMBERS:
      return state.set('members', fromJS(action.members));

    case SET_NAMESPACE_INFO:
      return state.set(
        'namespaceInfo',
        fromJS({
          loading: false,
          data: action.infos,
        })
      );

    case SET_RESOURCE_POOLS:
      return state.set(
        'resourcePools',
        fromJS({
          loading: false,
          data: action.resourcePools,
        })
      );

    case SET_ACTIVE_TOPIC:
      return state.set('activeTopic', action.activeTopic);

    case SET_ACTIVE_APPLICATION:
      return state.set('activeApplication', action.activeApplication);

    case SET_ACTIVE_MODAL:
      return state.set('activeModal', action.activeModal);

    case REQUEST_APPROVAL:
      return state.set(
        'details',
        state.get('details').setIn(['approvals', action.approvalType, 'status'], {
          loading: true,
        })
      );

    case APPROVAL_SUCCESS:
      return state.set(
        'details',
        state.get('details').setIn(['approvals', action.approvalType], {
          ...action.approval[action.approvalType],
          status: {
            success: true,
          },
        })
      );

    case APPROVAL_FAILURE:
      return state.set(
        'details',
        state.get('details').setIn(['approvals', action.approvalType, 'status'], {
          success: false,
          error: action.error,
        })
      );

    case TOPIC_REQUEST_SUCCESS:
      return state.set('activeModal', false);

    case SIMPLE_MEMBER_REQUEST_COMPLETE:
      return state.set('activeModal', false).setIn(['notification', 'message'], '');

    case REQUEST_REMOVE_MEMBER:
      return state.set(
        'members',
        state.get('members').map((m: any) =>
          (m.toJS() as Member).distinguished_name === action.distinguished_name
            ? m.set('removeStatus', {
                loading: true,
              })
            : m
        )
      );

    case REMOVE_MEMBER_SUCCESS:
      return state.set(
        'members',
        state.get('members').map((m: any) =>
          (m.toJS() as Member).distinguished_name === action.distinguished_name
            ? m.set('removeStatus', {
                success: true,
              })
            : m
        )
      );

    case REMOVE_MEMBER_FAILURE:
      return state.set(
        'members',
        state.get('members').map((m: any) =>
          (m.toJS() as Member).distinguished_name === action.distinguished_name
            ? m.set('removeStatus', {
                success: false,
                error: action.error,
              })
            : m
        )
      );

    case REQUEST_REFRESH_YARN_APPS:
      return state.set(
        'resourcePools',
        state
          .get('resourcePools')
          .setIn(['loading'], true)
          .setIn(['error'], '')
      );

    case REFRESH_YARN_APPS_SUCCESS:
      return state.set(
        'resourcePools',
        fromJS({
          loading: false,
          data: action.apps,
        })
      );

    case REFRESH_YARN_APPS_FAILURE:
      return state.set(
        'resourcePools',
        state
          .get('resourcePools')
          .setIn(['loading'], false)
          .setIn(['error'], action.error)
      );

    case REQUEST_REFRESH_HIVE_TABLES:
      return state.set(
        'namespaceInfo',
        state
          .get('namespaceInfo')
          .setIn(['loading'], true)
          .setIn(['error'], '')
      );

    case REFRESH_HIVE_TABLES_SUCCESS:
      return state.set(
        'namespaceInfo',
        fromJS({
          loading: false,
          data: action.tables,
        })
      );

    case REFRESH_HIVE_TABLES_FAILURE:
      return state.set(
        'namespaceInfo',
        state
          .get('namespaceInfo')
          .setIn(['loading'], false)
          .setIn(['error'], action.error)
      );

    case SET_MEMBER_LOADING:
      return state.set('memberLoading', action.loading);

    case SET_PROVISIONING_STATUS:
      return state.set('provisioning', action.provisioning);

    case SET_NOTIFICATION_STATUS:
      return state.set('notification', fromJS({ type: action.payload.type, message: action.payload.message }));

    case MANAGE_LOADING:
      return state.setIn(['manageLoading', action.payload.manageType], action.payload.loading);

    case CLEAR_NOTIFICATION_STATUS:
      return state.set('notification', fromJS({ type: '', message: '' }));

    case SET_WORKSPACE_FETCHING:
      return state.set('fetching', action.fetching);

    case SET_USERSUGGESTIONS_LOADING:
      return state.set('userSuggestionsLoading', action.loading);

    case SET_OWNER_LOADING:
      return state.set('ownerLoading', action.loading);

    case SET_QUOTA_LOADING:
      return state.set('quotaLoading', action.loading);

    default:
      return state;
  }
};

export default details;
