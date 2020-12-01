import { createSelector } from 'reselect';
import { workspaceListSelector, authSelector } from '../../redux/selectors';
import { Profile } from '../../models/Profile';
import { SearchBarSelector } from '../../redux/selectors';

export const SearchBar = new SearchBarSelector(workspaceListSelector);

export const getProfile = () =>
  createSelector(
    authSelector,
    authState => authState.get('profile') as Profile
  );
