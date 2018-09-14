import { Avatar } from 'antd';
import * as React from 'react';
import { connect } from "react-redux";
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { requestLogout } from "../Auth/actions";
import { Profile } from '../types/Profile';
import { getProfile } from '../selectors';


interface Props {
  profile: Profile,
  doLogout: () => void
}

const Profile = ({profile, doLogout}: Props) =>  {
    let name;
    if (profile && profile.name)
        name = profile.name;

    return (
        <div style={{ width: '100%', padding: 15, color: 'white', textAlign: 'center', borderTop: '1px solid #C6CACF', borderBottom: '1px solid #C6CACF', backgroundColor: '#415161' }}>
            <Avatar shape="circle" size="large" icon="user" style={{ backgroundColor: 'transparent', color: 'white' }} />
            <h3 style={{ color: 'white' }}>
                hey, {name}!
            </h3>
            <h6>
              <a href="#" style={{ textTransform: 'uppercase', fontWeight: 200, color: 'white' }} onClick={doLogout}>
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
  doLogout: () => dispatch(requestLogout())
});

export default connect(mapStateToProps, mapDispatchToProps)(Profile);
