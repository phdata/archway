import { CustomDescription } from '../../models/Template';

export const SET_CUSTOM_DESCRIPTIONS = 'SET_CUSTOM_DESCRIPTIONS';
export const LIST_CUSTOM_DESCRIPTIONS = 'LIST_CUSTOM_DESCRIPTIONS';

export const setCustomDescriptions = (customDescriptions: CustomDescription[]) => ({
  type: SET_CUSTOM_DESCRIPTIONS,
  customDescriptions,
});

export const listCustomDescriptions = () => ({
  type: LIST_CUSTOM_DESCRIPTIONS,
});
