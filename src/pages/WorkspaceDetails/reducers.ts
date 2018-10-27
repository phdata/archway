import { fromJS } from 'immutable';
import {
  GET_WORKSPACE,
  SET_WORKSPACE,
  SET_MEMBERS,
  SET_NAMESPACE_INFO,
  SET_RESOURCE_POOLS,
  SET_ACTIVE_MODAL,
  REQUEST_APPROVAL,
  APPROVAL_SUCCESS,
  TOPIC_REQUEST_SUCCESS,
  SIMPLE_MEMBER_REQUEST_COMPLETE,
  APPROVAL_FAILURE,
} from './actions';

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

    case SET_MEMBERS:
      return state
        .set('members', fromJS(action.members));

    case SET_NAMESPACE_INFO:
      return state
        .set('namespaceInfo', fromJS(action.infos));

    case SET_RESOURCE_POOLS:
      return state
        .set('resourcePools', fromJS(action.resourcePools));

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

    default:
      return state;
  }
};

export default details;
