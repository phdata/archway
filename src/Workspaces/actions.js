export const CHANGE_ACTIVE_WORKSPACE = 'CHANGE_ACTIVE_WORKSPACE';

export function changeActiveWorkspace(workspace) {
  return {
    type: CHANGE_ACTIVE_WORKSPACE,
    workspace,
  };
}
