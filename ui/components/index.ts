export { default as Behavior } from './Behavior';
export { default as Colors } from './Colors';
export { default as FieldLabel } from './FieldLabel';
export { default as WorkspaceListItem } from './WorkspaceListItem';
export { default as ListCardToggle } from './ListCardToggle';
export { default as Main } from './Main';
export { default as ListingSearchBar } from './ListingSearchBar';
export { default as WorkspaceList } from './WorkspaceList';

export const workspaceColumns = [
  {
    title: 'Name',
    dataIndex: 'name',
    key: 'name',
    width: 160,
  },
  {
    title: 'Summary',
    dataIndex: 'summary',
    key: 'summary',
  },
  {
    title: 'Behavior',
    dataIndex: 'behavior',
    key: 'behavior',
    width: 100,
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: 'Description',
    dataIndex: 'description',
    key: 'description',
  },
];
