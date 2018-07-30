export const APPROVE_WORKSPACE_REQUESTED = 'APPROVE_WORKSPACE_REQUESTED';

export function approveInfra() {
  return {
    type: APPROVE_WORKSPACE_REQUESTED,
    role: 'infra',
  };
}

export function approveRisk() {
  return {
    type: APPROVE_WORKSPACE_REQUESTED,
    role: 'risk',
  };
}

export const APPROVE_WORKSPACE_COMPLETED = 'APPOVE_WORKSPACE_COMPLETED';

export function approveWorkspaceCompleted(error) {
  return {
    type: APPROVE_WORKSPACE_COMPLETED,
    error,
  };
}
