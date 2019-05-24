import { Avatar } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { requestLogout } from '../../Login/actions';
import { getProfile } from '../../../redux/selectors';
import { Profile } from '../../../models/Profile';

interface Props {
    profile: Profile;
    doLogout: () => void;
}

const ProfileComponent = ({ profile, doLogout }: Props) => {
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
      }}>
      <Avatar shape="circle" size="large" icon="user" style={{ backgroundColor: 'transparent', color: 'white' }} />
      <h3 style={{ color: 'white', textTransform: 'uppercase' }}>
        {name}
      </h3>
      <h6>
        <a href="#" style={{ textTransform: 'uppercase', fontWeight: 200, color: 'white' }} onClick={doLogout}> {/* eslint-disable-line */}
          log out
        </a>
      </h6>
    </div>
  );
};

const mapStateToProps = () =>
  createStructuredSelector({
    profile: getProfile(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  doLogout: () => dispatch(requestLogout()),
});

export default connect(mapStateToProps, mapDispatchToProps)(ProfileComponent);
