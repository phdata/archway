import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';

export const SET_BEHAVIOR = 'SET_BEHAVIOR';
export function setBehavior(behavior: string) {
  return {
    type: 'SET_BEHAVIOR',
    behavior,
  };
}

export const SET_WORKSPACE = 'WORKSPACE_GENERATED';
export function workspaceGenerated(workspace: Workspace) {
  return {
    type: SET_WORKSPACE,
    workspace,
  };
}

export const SET_REQUEST = 'SET_REQUEST';
export function setRequest(request: RequestInput) {
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

export const SET_GENERATING = 'SET_GENERATING';
export function setGenerating(generating: boolean) {
  return {
    type: SET_GENERATING,
    generating,
  };
}

export const WORKSPACE_REQUESTED = 'WORKSPACE_REQUESTED';
export function workspaceRequested() {
  return {
    type: WORKSPACE_REQUESTED,
  };
}

export const SET_PAGE_NUMBER = 'SET_PAGE_NUMBER';
export function setPage(page: number) {
  return {
    type: SET_PAGE_NUMBER,
    page,
  };
}
