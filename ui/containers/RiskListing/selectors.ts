import { riskSelector, SearchBarSelector } from '../../redux/selectors';

export const searchBar = new SearchBarSelector(riskSelector);
