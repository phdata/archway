import { opsSelector, SearchBarSelector } from '../../redux/selectors';

export const SearchBar = new SearchBarSelector(opsSelector);
