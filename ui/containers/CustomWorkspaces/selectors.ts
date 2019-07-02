import { createSelector } from 'reselect';
import { templatesSelector } from '../../redux/selectors';

export const getCustomDescriptions = () =>
  createSelector(
    templatesSelector,
    templatesState => templatesState.get('customDescriptions').toJS()
  );
