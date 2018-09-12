export const filterWorkspaces = 
  (filter: string, behavior: string[]) => ({
    type: 'FILTER_WORKSPACES',
    filter,
    behavior
});