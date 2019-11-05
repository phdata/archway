import * as React from 'react';
import { InjectedFormProps } from 'redux-form';
import { reduxForm } from 'redux-form/immutable';

import { UserSuggestions } from '../../../../models/Workspace';
import { MemberForm } from '../../../../models/Form';
import MemberSearchBar from '../MemberSearchBar';
import { ShowTypes } from '../../constants';

interface ChangeOwnerRequestProps {
  suggestions?: UserSuggestions;
  loading: boolean;
  onSearch?: (v: string) => void;
  handleSubmit?: () => void;
}

const ChangeOwnerRequest = ({
  suggestions,
  loading,
  onSearch,
  handleSubmit,
}: InjectedFormProps<MemberForm, {}> & ChangeOwnerRequestProps) => (
  <form onSubmit={handleSubmit}>
    <MemberSearchBar loading={loading} suggestions={suggestions} showTypes={[ShowTypes.Users]} onSearch={onSearch} />
  </form>
);

export default reduxForm<MemberForm, ChangeOwnerRequestProps>({
  form: 'changeOwnerRequest',
  initialValues: { distinguishedName: '', roles: {} },
})(ChangeOwnerRequest);
