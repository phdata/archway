import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';

export const SET_LOADING = 'SET_LOADING';
export function setLoading(loading: boolean) {
  return {
    type: SET_LOADING,
    loading,
  };
}

export const SET_BEHAVIOR = 'SET_BEHAVIOR';
export function setBehavior(behavior: string) {
  return {
    type: 'SET_BEHAVIOR',
    behavior,
  };
}

export const SET_REQUEST = 'SET_REQUEST';
export function setRequest(request: boolean | RequestInput) {
  return {
    type: SET_REQUEST,
    request,
  };
}

export const SET_TEMPLATE = 'SET_TEMPLATE';
export function setTemplate(template: RequestInput) {
  return {
    type: SET_TEMPLATE,
    template,
  };
}

export const SET_WORKSPACE = 'SET_WORKSPACE';
export function setWorkspace(workspace: Workspace) {
  return {
    type: SET_WORKSPACE,
    workspace,
  };
}

export const SET_CURRENT_PAGE = 'SET_CURRENT_PAGE';
export function setCurrentPage(page: string) {
  return {
    type: SET_CURRENT_PAGE,
    page,
  };
}

export const GOTO_NEXT_PAGE = 'GOTO_NEXT_PAGE';
export function gotoNextPage() {
  return {
    type: GOTO_NEXT_PAGE,
  };
}

export const GOTO_PREV_PAGE = 'GOTO_PREV_PAGE';
export function gotoPrevPage() {
  return {
    type: GOTO_PREV_PAGE,
  };
}

export const CREATE_WORKSPACE_REQUEST = 'CREATE_WORKSPACE_REQUEST';
export const createWorkspaceRequest = () => ({
  type: CREATE_WORKSPACE_REQUEST,
});

export const CREATE_WORKSPACE_FAILURE = 'CREATE_WORKSPACE_FAILURE';
export const createWorkspaceFailure = (error: string) => ({
  type: CREATE_WORKSPACE_FAILURE,
  error,
});

export const SET_ADVANCED_VISIBLE = 'SET_ADVANCED_VISIBLE';
export function setAdvancedVisible(visible: boolean) {
  return {
    type: SET_ADVANCED_VISIBLE,
    visible,
  };
}

export const CLEAR_REQUEST = 'CLEAR_REQUEST';
export function clearRequest() {
  return {
    type: CLEAR_REQUEST,
  };
}

export const CLEAR_ERROR = 'CLEAR_ERROR';
export function clearError() {
  return {
    type: CLEAR_ERROR,
  };
}
