import { Avatar } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { requestLogout } from '../../Login/actions';
import { getProfile } from '../../../redux/selectors';
import { getAuthType } from '../../Login/selectors';
import { Profile } from '../../../models/Profile';
import { SPNEGO } from '../../../constants';

interface Props {
  profile: Profile;
  authType: string;
  doLogout: () => void;
}

const ProfileComponent = ({ profile, authType, doLogout }: Props) => {
  const isSpnego: boolean = authType === SPNEGO;
  let name;
  if (profile && profile.name) {
    name = profile.name;
  }

  return (
    <div
      style={{
        width: '100%',
        padding: 15,
        color: 'white',
        textAlign: 'center',
        backgroundColor: '#415161',
        position: 'absolute' as 'absolute', // https://github.com/Microsoft/TypeScript/issues/11465
        bottom: 0,
      }}
    >
      <Avatar shape="circle" size="large" icon="user" style={{ backgroundColor: 'transparent', color: 'white' }} />
      <h3 style={{ color: 'white', textTransform: 'uppercase' }}>{name}</h3>
      {isSpnego || (
        <h6>
          <a href="#" style={{ textTransform: 'uppercase', fontWeight: 200, color: 'white' }} onClick={doLogout}>
            {' '}
            {/* eslint-disable-line */}
            log out
          </a>
        </h6>
      )}
    </div>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    profile: getProfile(),
    authType: getAuthType(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  doLogout: () => dispatch(requestLogout()),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(ProfileComponent);
